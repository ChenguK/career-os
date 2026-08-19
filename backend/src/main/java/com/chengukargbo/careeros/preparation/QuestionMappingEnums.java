package com.chengukargbo.careeros.preparation;

public final class QuestionMappingEnums {
    private QuestionMappingEnums() {}
    public enum MappingSource { EXACT_EXTERNAL_KEY, EXACT_TEXT, USER, ADAPTER }
    public enum MappingState { UNCONFIRMED, CONFIRMED, REVOKED }
    public enum MappingEventType { CONFIRMED, CHANGED, REVOKED }
}
