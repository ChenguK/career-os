package com.chengukargbo.careeros.answers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.answers.dto.ApprovedAnswerRequest;
import com.chengukargbo.careeros.answers.dto.ApprovedAnswerResponse;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.profile.ApplicantProfile;
import com.chengukargbo.careeros.profile.ApplicantProfileRepository;

@Service
@Transactional
public class ApprovedAnswerService {

    private static final String WORK_AUTHORIZATION = "work_authorization";
    private static final String SPONSORSHIP_REQUIRED = "sponsorship_required";

    private final ApprovedAnswerRepository answerRepository;
    private final ApplicantProfileRepository profileRepository;

    public ApprovedAnswerService(
        ApprovedAnswerRepository answerRepository,
        ApplicantProfileRepository profileRepository
    ) {
        this.answerRepository = answerRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<ApprovedAnswerResponse> findAll() {
        ApplicantProfile profile = currentProfile();
        return answerRepository.findAllByOrderByCanonicalKeyAsc()
            .stream()
            .map(answer -> response(answer, profile))
            .toList();
    }

    public ApprovedAnswerResponse create(ApprovedAnswerRequest request) {
        Values values = validateAndNormalize(request, null);
        ApprovedAnswer answer = new ApprovedAnswer(
            values.canonicalKey(), values.representativeQuestion(),
            values.answerType(), values.textValue(), values.booleanValue(),
            values.numberValue(), values.classification(), values.reusable(),
            values.answerSource(), values.profileField(), values.notes()
        );
        return response(answerRepository.saveAndFlush(answer), currentProfile());
    }

    public ApprovedAnswerResponse update(
        Long id,
        ApprovedAnswerRequest request
    ) {
        ApprovedAnswer answer = findEntity(id);
        Values values = validateAndNormalize(request, id);
        answer.update(
            values.canonicalKey(), values.representativeQuestion(),
            values.answerType(), values.textValue(), values.booleanValue(),
            values.numberValue(), values.classification(), values.reusable(),
            values.answerSource(), values.profileField(), values.notes()
        );
        return response(answerRepository.saveAndFlush(answer), currentProfile());
    }

    public ApprovedAnswerResponse approve(Long id) {
        ApprovedAnswer answer = findEntity(id);
        if (answer.getClassification() == AnswerClassification.UNKNOWN) {
            throw new BusinessValidationException(
                "Unknown answers cannot be approved"
            );
        }
        ApplicantProfile profile = currentProfile();
        if (answer.getAnswerSource() == AnswerSource.APPLICANT_PROFILE
            && !profileAuthorityAvailable(answer, profile)) {
            throw new BusinessValidationException(
                "Verify the Applicant Profile and provide the source value before approving this answer"
            );
        }
        answer.approve();
        return response(answerRepository.saveAndFlush(answer), profile);
    }

    public ApprovedAnswerResponse revoke(Long id) {
        ApprovedAnswer answer = findEntity(id);
        answer.revokeApproval();
        return response(
            answerRepository.saveAndFlush(answer), currentProfile()
        );
    }

    public void delete(Long id) {
        answerRepository.delete(findEntity(id));
    }

    private ApprovedAnswer findEntity(Long id) {
        return answerRepository.findById(id)
            .orElseThrow(() -> new ApprovedAnswerNotFoundException(id));
    }

    private Values validateAndNormalize(
        ApprovedAnswerRequest request,
        Long existingId
    ) {
        String key = request.canonicalKey().trim().toLowerCase(Locale.ROOT);
        if (key.equals(WORK_AUTHORIZATION) || key.equals(SPONSORSHIP_REQUIRED)) {
            throw new BusinessValidationException(
                "Generic work authorization and sponsorship answers are intentionally unsupported"
            );
        }
        boolean duplicate = existingId == null
            ? answerRepository.existsByCanonicalKey(key)
            : answerRepository.existsByCanonicalKeyAndIdNot(key, existingId);
        if (duplicate) {
            throw new BusinessValidationException(
                "An approved answer already uses canonical key " + key
            );
        }
        if (request.reusable()
            && request.classification()
                != AnswerClassification.VERIFIED_REUSABLE) {
            throw new BusinessValidationException(
                "Only VERIFIED_REUSABLE answers may be marked reusable"
            );
        }

        String textValue = normalize(request.textValue());
        Boolean booleanValue = request.booleanValue();
        BigDecimal numberValue = request.numberValue();
        ProfileAnswerField profileField = request.profileField();

        if (request.answerSource() == AnswerSource.APPLICANT_PROFILE) {
            validateProfileMapping(key, request.answerType(), profileField);
            if (textValue != null || booleanValue != null || numberValue != null) {
                throw new BusinessValidationException(
                    "Profile-backed answers must not store copied values"
                );
            }
        } else {
            if (key.equals("willing_to_relocate")
                || key.equals("willing_to_travel")
                || key.equals("salary_expectation")) {
                throw new BusinessValidationException(
                    "This canonical key must reference the authoritative Applicant Profile value"
                );
            }
            if (profileField != null) {
                throw new BusinessValidationException(
                    "Manual answers cannot reference an Applicant Profile field"
                );
            }
            validateManualValue(
                request.answerType(), textValue, booleanValue, numberValue
            );
        }

        return new Values(
            key,
            request.representativeQuestion().trim(),
            request.answerType(),
            textValue,
            booleanValue,
            numberValue,
            request.classification(),
            request.reusable(),
            request.answerSource(),
            profileField,
            normalize(request.notes())
        );
    }

    private void validateProfileMapping(
        String key,
        AnswerType type,
        ProfileAnswerField field
    ) {
        boolean valid = switch (key) {
            case "willing_to_relocate" ->
                field == ProfileAnswerField.WILLING_TO_RELOCATE
                    && type == AnswerType.BOOLEAN;
            case "willing_to_travel" ->
                field == ProfileAnswerField.WILLING_TO_TRAVEL
                    && type == AnswerType.BOOLEAN;
            case "salary_expectation" ->
                field == ProfileAnswerField.MINIMUM_SALARY
                    && type == AnswerType.NUMBER;
            default -> false;
        };
        if (!valid) {
            throw new BusinessValidationException(
                "Unsupported Applicant Profile answer mapping"
            );
        }
    }

    private void validateManualValue(
        AnswerType type,
        String text,
        Boolean bool,
        BigDecimal number
    ) {
        boolean valid = switch (type) {
            case TEXT -> text != null && bool == null && number == null;
            case BOOLEAN -> text == null && bool != null && number == null;
            case NUMBER -> text == null && bool == null && number != null;
        };
        if (!valid) {
            throw new BusinessValidationException(
                "Provide exactly one value matching the selected answer type"
            );
        }
    }

    private ApprovedAnswerResponse response(
        ApprovedAnswer answer,
        ApplicantProfile profile
    ) {
        if (answer.getAnswerSource() == AnswerSource.MANUAL) {
            return ApprovedAnswerResponse.from(
                answer, true, answer.getTextValue(), answer.getBooleanValue(),
                answer.getNumberValue(), null
            );
        }

        Boolean bool = null;
        BigDecimal number = null;
        String currency = null;
        if (profile != null) {
            switch (answer.getProfileField()) {
                case WILLING_TO_RELOCATE ->
                    bool = profile.getWillingToRelocate();
                case WILLING_TO_TRAVEL ->
                    bool = profile.getWillingToTravel();
                case MINIMUM_SALARY -> {
                    number = profile.getMinimumSalary();
                    currency = profile.getSalaryCurrency();
                }
            }
        }
        return ApprovedAnswerResponse.from(
            answer,
            profileAuthorityAvailable(answer, profile),
            null,
            bool,
            number,
            currency
        );
    }

    private boolean profileAuthorityAvailable(
        ApprovedAnswer answer,
        ApplicantProfile profile
    ) {
        if (profile == null || !profile.isVerified()) {
            return false;
        }
        return switch (answer.getProfileField()) {
            case WILLING_TO_RELOCATE -> profile.getWillingToRelocate() != null;
            case WILLING_TO_TRAVEL -> profile.getWillingToTravel() != null;
            case MINIMUM_SALARY -> profile.getMinimumSalary() != null;
        };
    }

    private ApplicantProfile currentProfile() {
        return profileRepository
            .findByProfileKey(ApplicantProfile.PRIMARY_PROFILE_KEY)
            .orElse(null);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record Values(
        String canonicalKey,
        String representativeQuestion,
        AnswerType answerType,
        String textValue,
        Boolean booleanValue,
        BigDecimal numberValue,
        AnswerClassification classification,
        boolean reusable,
        AnswerSource answerSource,
        ProfileAnswerField profileField,
        String notes
    ) {
    }
}
