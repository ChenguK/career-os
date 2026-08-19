package com.chengukargbo.careeros.preparation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.*;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.common.url.ApplicationUrlService;
import com.chengukargbo.careeros.preparation.ObservationDtos.*;

@Service
@Transactional
public class FormObservationService {
    private final ApplicationRepository applications;
    private final ApplicationFormTargetRepository targets;
    private final FormObservationSnapshotRepository snapshots;
    private final ObservedQuestionRepository questions;
    private final ObservedMaterialRequirementRepository materialRequirements;
    private final ApplicationUrlService urls;

    public FormObservationService(
        ApplicationRepository applications,
        ApplicationFormTargetRepository targets,
        FormObservationSnapshotRepository snapshots,
        ObservedQuestionRepository questions,
        ObservedMaterialRequirementRepository materialRequirements,
        ApplicationUrlService urls
    ) {
        this.applications = applications;
        this.targets = targets;
        this.snapshots = snapshots;
        this.questions = questions;
        this.materialRequirements = materialRequirements;
        this.urls = urls;
    }

    // Internal adapter contract. No HTTP write endpoint is exposed.
    public SnapshotResponse reconcile(Long applicationId, SnapshotInput input) {
        return reconcile(applicationId, null, input);
    }

    public SnapshotResponse reconcile(Long applicationId,
        ApplicationPreparationSession preparationSession,
        SnapshotInput input) {
        application(applicationId);
        ApplicationFormTarget target = targets.findByApplicationId(applicationId)
            .orElseThrow(() -> new BusinessValidationException(
                "Initialize preparation before recording form observations"
            ));
        confirmIdentity(target, input == null ? null : input.identity());
        List<QuestionSpec> incoming = normalize(input);
        List<MaterialRequirementSpec> incomingMaterials = normalizeMaterials(input);
        FormObservationSnapshot previous = snapshots
            .findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(
                applicationId
            ).orElse(null);
        Map<String, ObservedQuestion> previousById = previous == null
            ? Map.of() : index(previous.getQuestions());

        List<QuestionSpec> complete = new ArrayList<>(incoming);
        Set<String> incomingIds = new HashSet<>();
        incoming.forEach(question -> incomingIds.add(question.externalId()));
        previousById.values().stream()
            .filter(ObservedQuestion::isActive)
            .filter(question -> !incomingIds.contains(question.getExternalQuestionId()))
            .map(this::inactive)
            .forEach(complete::add);
        complete.sort(Comparator.comparingInt(QuestionSpec::displayOrder)
            .thenComparing(QuestionSpec::externalId));

        int sequence = previous == null ? 1 : previous.getSequenceNumber() + 1;
        FormObservationSnapshot snapshot = new FormObservationSnapshot(
            target, preparationSession, sequence,
            hash(target.getNormalizedFormUrl(),
                Objects.toString(target.getExternalRequisitionId(), ""),
                Objects.toString(target.getExternalFormKey(), ""),
                fingerprint(complete), incomingMaterials.toString())
        );
        for (QuestionSpec spec : complete) {
            ObservedQuestion prior = previousById.get(spec.externalId());
            List<OptionSpec> reconciledOptions = reconcileOptions(spec, prior);
            ObservedQuestion question = new ObservedQuestion(
                snapshot, spec.externalId(), spec.text(), spec.answerType(),
                spec.required(), spec.pageKey(), spec.active(), spec.displayOrder(),
                questionFingerprint(spec, reconciledOptions)
            );
            reconciledOptions.forEach(option -> question.add(new ObservedOption(
                question, option.externalId(), option.value(), option.label(),
                option.displayOrder(), option.active()
            )));
            snapshot.add(question);
        }
        incomingMaterials.forEach(requirement -> snapshot.add(
            new ObservedMaterialRequirement(
                snapshot, requirement.externalId(), requirement.materialType(),
                requirement.label(), requirement.required(), requirement.acceptTypes(),
                requirement.displayOrder(), requirement.pageKey()
            )
        ));
        return SnapshotResponse.from(snapshots.saveAndFlush(snapshot));
    }

    @Transactional(readOnly = true)
    public List<SnapshotResponse> snapshots(Long applicationId) {
        application(applicationId);
        return snapshots.findByFormTargetApplicationIdOrderBySequenceNumberDesc(
            applicationId
        ).stream().map(SnapshotResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> latestQuestions(Long applicationId) {
        application(applicationId);
        return snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(
            applicationId
        ).map(snapshot -> questionResponses(snapshot.getId()))
            .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> snapshotQuestions(
        Long applicationId, Long snapshotId
    ) {
        application(applicationId);
        FormObservationSnapshot snapshot = snapshots
            .findByIdAndFormTargetApplicationId(snapshotId, applicationId)
            .orElseThrow(() -> new BusinessValidationException(
                "Form observation snapshot not found"
            ));
        return questionResponses(snapshot.getId());
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirementResponse> latestMaterialRequirements(Long applicationId) {
        application(applicationId);
        return snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(applicationId)
            .map(snapshot -> materialRequirementResponses(snapshot.getId()))
            .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirementResponse> snapshotMaterialRequirements(
        Long applicationId, Long snapshotId
    ) {
        application(applicationId);
        FormObservationSnapshot snapshot = snapshots
            .findByIdAndFormTargetApplicationId(snapshotId, applicationId)
            .orElseThrow(() -> new BusinessValidationException(
                "Form observation snapshot not found"
            ));
        return materialRequirementResponses(snapshot.getId());
    }

    private List<MaterialRequirementResponse> materialRequirementResponses(Long snapshotId) {
        return materialRequirements
            .findBySnapshotIdOrderByDisplayOrderAscExternalFieldIdAsc(snapshotId)
            .stream().map(MaterialRequirementResponse::from).toList();
    }

    private List<QuestionResponse> questionResponses(Long snapshotId) {
        return questions
            .findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(snapshotId)
            .stream().map(QuestionResponse::from).toList();
    }

    private List<QuestionSpec> normalize(SnapshotInput input) {
        if (input == null || input.questions() == null) {
            throw new BusinessValidationException(
                "Observed questions are required"
            );
        }
        Set<String> ids = new HashSet<>();
        List<QuestionSpec> result = new ArrayList<>();
        for (QuestionInput question : input.questions()) {
            String id = required(question.externalQuestionId(),
                "External question ID is required", 200);
            if (!ids.add(id)) {
                throw new BusinessValidationException(
                    "Duplicate external question ID: " + id
                );
            }
            if (question.answerType() == null) {
                throw new BusinessValidationException("Question answer type is required");
            }
            result.add(new QuestionSpec(
                id, required(question.questionText(), "Question text is required", 1000),
                question.answerType(), question.required(),
                required(question.pageKey(), "Question page key is required", 200),
                question.displayOrder(),
                normalizeOptions(question.options()), true
            ));
        }
        result.sort(Comparator.comparingInt(QuestionSpec::displayOrder)
            .thenComparing(QuestionSpec::externalId));
        return result;
    }

    private void confirmIdentity(ApplicationFormTarget target,
        FormIdentityInput input) {
        if (input == null) return;
        String normalizedUrl = urls.normalize(input.normalizedFormUrl());
        if (normalizedUrl == null) {
            throw new BusinessValidationException(
                "Observed form identity requires a valid form URL"
            );
        }
        target.observeIdentity(
            normalizedUrl,
            optional(input.externalRequisitionId(), 200),
            optional(input.externalFormKey(), 200)
        );
        targets.save(target);
    }

    private List<OptionSpec> normalizeOptions(List<OptionInput> input) {
        if (input == null) return List.of();
        Set<String> values = new HashSet<>();
        Set<String> externalIds = new HashSet<>();
        List<OptionSpec> result = new ArrayList<>();
        for (OptionInput option : input) {
            String value = required(option.value(), "Option value is required", 1000);
            String externalId = optional(option.externalOptionId(), 200);
            if (!values.add(value)) {
                throw new BusinessValidationException("Duplicate option value: " + value);
            }
            if (externalId != null && !externalIds.add(externalId)) {
                throw new BusinessValidationException(
                    "Duplicate external option ID: " + externalId
                );
            }
            result.add(new OptionSpec(
                externalId, value,
                required(option.label(), "Option label is required", 1000),
                option.displayOrder(), true
            ));
        }
        result.sort(Comparator.comparingInt(OptionSpec::displayOrder)
            .thenComparing(OptionSpec::value));
        return result;
    }

    private List<MaterialRequirementSpec> normalizeMaterials(SnapshotInput input) {
        if (input == null || input.materialRequirements() == null) return List.of();
        Set<String> ids = new HashSet<>();
        List<MaterialRequirementSpec> result = new ArrayList<>();
        for (MaterialRequirementInput requirement : input.materialRequirements()) {
            String id = required(requirement.externalFieldId(),
                "External material field ID is required", 200);
            if (!ids.add(id)) {
                throw new BusinessValidationException(
                    "Duplicate external material field ID: " + id
                );
            }
            if (requirement.materialType() == null) {
                throw new BusinessValidationException("Material type is required");
            }
            result.add(new MaterialRequirementSpec(
                id, requirement.materialType(),
                required(requirement.label(), "Material field label is required", 1000),
                requirement.required(), optional(requirement.acceptTypes(), 1000),
                requirement.displayOrder(),
                required(requirement.pageKey(), "Material page key is required", 200)
            ));
        }
        result.sort(Comparator.comparingInt(MaterialRequirementSpec::displayOrder)
            .thenComparing(MaterialRequirementSpec::externalId));
        return result;
    }

    private List<OptionSpec> reconcileOptions(QuestionSpec current,
        ObservedQuestion prior) {
        List<OptionSpec> result = new ArrayList<>(current.options());
        if (prior != null && current.active()) {
            Set<String> currentValues = new HashSet<>();
            current.options().forEach(option -> currentValues.add(option.value()));
            prior.getOptions().stream().filter(ObservedOption::isActive)
                .filter(option -> !currentValues.contains(option.getOptionValue()))
                .map(option -> new OptionSpec(
                    option.getExternalOptionId(), option.getOptionValue(),
                    option.getOptionLabel(), option.getDisplayOrder(), false
                )).forEach(result::add);
        }
        result.sort(Comparator.comparingInt(OptionSpec::displayOrder)
            .thenComparing(OptionSpec::value));
        return result;
    }

    private QuestionSpec inactive(ObservedQuestion question) {
        return new QuestionSpec(
            question.getExternalQuestionId(), question.getQuestionText(),
            question.getAnswerType(), question.isRequired(),
            question.getPageKey(), question.getDisplayOrder(), question.getOptions().stream()
                .map(option -> new OptionSpec(
                    option.getExternalOptionId(), option.getOptionValue(),
                    option.getOptionLabel(), option.getDisplayOrder(), false
                )).toList(), false
        );
    }

    private Map<String, ObservedQuestion> index(List<ObservedQuestion> values) {
        Map<String, ObservedQuestion> result = new HashMap<>();
        values.forEach(value -> result.put(value.getExternalQuestionId(), value));
        return result;
    }

    private String questionFingerprint(QuestionSpec question,
        List<OptionSpec> options) {
        return hash(question.externalId(), question.text(),
            question.answerType().name(), Boolean.toString(question.required()),
            question.pageKey(), Boolean.toString(question.active()), options.toString());
    }

    private String fingerprint(List<QuestionSpec> questions) {
        return hash(questions.toString());
    }

    private String hash(String... values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String value : values) {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) ':');
                digest.update(bytes);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String required(String value, String message, int max) {
        String result = optional(value, max);
        if (result == null) throw new BusinessValidationException(message);
        return result;
    }

    private String optional(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        if (result.length() > max) {
            throw new BusinessValidationException(
                "Observed form value must not exceed " + max + " characters"
            );
        }
        return result;
    }

    private Application application(Long id) {
        return applications.findById(id)
            .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private record QuestionSpec(
        String externalId, String text,
        com.chengukargbo.careeros.questions.QuestionEnums.AnswerType answerType,
        boolean required, String pageKey, int displayOrder, List<OptionSpec> options,
        boolean active
    ) {}
    private record OptionSpec(
        String externalId, String value, String label,
        int displayOrder, boolean active
    ) {}
    private record MaterialRequirementSpec(
        String externalId, com.chengukargbo.careeros.preparation.PreparationEnums.MaterialType materialType,
        String label, boolean required, String acceptTypes, int displayOrder,
        String pageKey
    ) {}
}
