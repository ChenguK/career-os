package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;
import java.util.List;

import com.chengukargbo.careeros.questions.QuestionEnums.AnswerType;

public final class ObservationDtos {
    private ObservationDtos() {}

    public record OptionInput(
        String externalOptionId, String value, String label, int displayOrder
    ) {}
    public record QuestionInput(
        String externalQuestionId, String questionText, AnswerType answerType,
        boolean required, int displayOrder, List<OptionInput> options
    ) {}
    public record FormIdentityInput(
        String normalizedFormUrl,
        String externalRequisitionId,
        String externalFormKey
    ) {}
    public record SnapshotInput(
        FormIdentityInput identity,
        List<QuestionInput> questions
    ) {}

    public record SnapshotResponse(
        Long id, Long preparationSessionId, FormIdentity formIdentity, int sequenceNumber,
        String fingerprint, OffsetDateTime observedAt, int activeQuestionCount,
        int inactiveQuestionCount
    ) {
        static SnapshotResponse from(FormObservationSnapshot snapshot) {
            int active = (int) snapshot.getQuestions().stream()
                .filter(ObservedQuestion::isActive).count();
            return new SnapshotResponse(
                snapshot.getId(), snapshot.getPreparationSession() == null ? null
                    : snapshot.getPreparationSession().getId(),
                FormIdentity.from(snapshot.getFormTarget()),
                snapshot.getSequenceNumber(), snapshot.getSnapshotFingerprint(),
                snapshot.getObservedAt(), active,
                snapshot.getQuestions().size() - active
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
        String questionText, AnswerType answerType, boolean required,
        boolean active, int displayOrder, String fingerprint,
        List<OptionResponse> options
    ) {
        static QuestionResponse from(ObservedQuestion question) {
            return new QuestionResponse(
                question.getId(), question.getSnapshot().getId(),
                question.getExternalQuestionId(), question.getQuestionText(),
                question.getAnswerType(), question.isRequired(),
                question.isActive(), question.getDisplayOrder(),
                question.getQuestionFingerprint(), question.getOptions().stream()
                    .map(OptionResponse::from).toList()
            );
        }
    }
}
