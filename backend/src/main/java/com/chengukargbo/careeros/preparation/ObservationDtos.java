package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;
import java.util.List;

import com.chengukargbo.careeros.questions.QuestionEnums.AnswerType;
import com.chengukargbo.careeros.preparation.PreparationEnums.MaterialType;

public final class ObservationDtos {
    private ObservationDtos() {}

    public record OptionInput(
        String externalOptionId, String value, String label, int displayOrder
    ) {}
    public record QuestionInput(
        String externalQuestionId, String questionText, AnswerType answerType,
        boolean required, String pageKey, int displayOrder, List<OptionInput> options
    ) {
        public QuestionInput(String externalQuestionId, String questionText,
            AnswerType answerType, boolean required, int displayOrder,
            List<OptionInput> options) {
            this(externalQuestionId, questionText, answerType, required,
                "application", displayOrder, options);
        }
    }
    public record FormIdentityInput(
        String normalizedFormUrl,
        String externalRequisitionId,
        String externalFormKey
    ) {}
    public record MaterialRequirementInput(
        String externalFieldId, MaterialType materialType, String label,
        boolean required, String acceptTypes, int displayOrder, String pageKey
    ) {}
    public record SnapshotInput(
        FormIdentityInput identity,
        List<QuestionInput> questions,
        List<MaterialRequirementInput> materialRequirements
    ) {
        public SnapshotInput(FormIdentityInput identity, List<QuestionInput> questions) {
            this(identity, questions, List.of());
        }
    }

    public record SnapshotResponse(
        Long id, Long preparationSessionId, FormIdentity formIdentity, int sequenceNumber,
        String fingerprint, OffsetDateTime observedAt, int activeQuestionCount,
        int inactiveQuestionCount, int materialRequirementCount
    ) {
        public SnapshotResponse(Long id, Long preparationSessionId,
            FormIdentity formIdentity, int sequenceNumber, String fingerprint,
            OffsetDateTime observedAt, int activeQuestionCount,
            int inactiveQuestionCount) {
            this(id, preparationSessionId, formIdentity, sequenceNumber,
                fingerprint, observedAt, activeQuestionCount,
                inactiveQuestionCount, 0);
        }

        static SnapshotResponse from(FormObservationSnapshot snapshot) {
            int active = (int) snapshot.getQuestions().stream()
                .filter(ObservedQuestion::isActive).count();
            return new SnapshotResponse(
                snapshot.getId(), snapshot.getPreparationSession() == null ? null
                    : snapshot.getPreparationSession().getId(),
                FormIdentity.from(snapshot.getFormTarget()),
                snapshot.getSequenceNumber(), snapshot.getSnapshotFingerprint(),
                snapshot.getObservedAt(), active,
                snapshot.getQuestions().size() - active,
                snapshot.getMaterialRequirements().size()
            );
        }
    }

    public record MaterialRequirementResponse(
        Long id, Long snapshotId, String externalFieldId, MaterialType materialType,
        String label, boolean required, String acceptTypes, int displayOrder,
        String pageKey
    ) {
        static MaterialRequirementResponse from(ObservedMaterialRequirement requirement) {
            return new MaterialRequirementResponse(
                requirement.getId(), requirement.getSnapshot().getId(),
                requirement.getExternalFieldId(), requirement.getMaterialType(),
                requirement.getFieldLabel(), requirement.isRequired(),
                requirement.getAcceptTypes(), requirement.getDisplayOrder(),
                requirement.getPageKey()
            );
        }
    }

    public record OptionResponse(
        Long id, String externalOptionId, String value, String label,
        int displayOrder, boolean active
    ) {
        static OptionResponse from(ObservedOption option) {
            return new OptionResponse(
                option.getId(), option.getExternalOptionId(),
                option.getOptionValue(), option.getOptionLabel(),
                option.getDisplayOrder(), option.isActive()
            );
        }
    }

    public record QuestionResponse(
        Long id, Long snapshotId, String externalQuestionId,
        String questionText, AnswerType answerType, boolean required, String pageKey,
        boolean active, int displayOrder, String fingerprint,
        List<OptionResponse> options
    ) {
        static QuestionResponse from(ObservedQuestion question) {
            return new QuestionResponse(
                question.getId(), question.getSnapshot().getId(),
                question.getExternalQuestionId(), question.getQuestionText(),
                question.getAnswerType(), question.isRequired(), question.getPageKey(),
                question.isActive(), question.getDisplayOrder(),
                question.getQuestionFingerprint(), question.getOptions().stream()
                    .map(OptionResponse::from).toList()
            );
        }
    }
}
