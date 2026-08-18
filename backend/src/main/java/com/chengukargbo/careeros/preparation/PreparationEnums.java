package com.chengukargbo.careeros.preparation;

public final class PreparationEnums {
    private PreparationEnums() {}

    public enum PreparationCapability {
        NONE, SESSION_ONLY, INSPECTION, FIELD_PREPARATION, READY_FOR_REVIEW
    }

    public enum SessionState {
        INITIALIZED, OPENING, COLLECTING_QUESTIONS, WAITING_FOR_USER,
        PREPARING_FIELDS, READY_FOR_REVIEW, FAILED, CANCELLED;

        public boolean active() {
            return this != READY_FOR_REVIEW && this != FAILED
                && this != CANCELLED;
        }
    }

    public enum IdentitySource { USER, ADAPTER }

    public enum EventType {
        SESSION_INITIALIZED, SESSION_CANCELLED, SESSION_RETRY_INITIALIZED,
        FORM_OPENING, COLLECTING_QUESTIONS, OBSERVATION_CAPTURED,
        WAITING_FOR_USER, FIELD_PLAN_CREATED, FIELD_PREPARATION_COMPLETED,
        FIELD_PREPARATION_FAILED, REVIEW_GENERATED, SESSION_PAUSED,
        SESSION_RESUMED, SESSION_FAILED
    }
}
