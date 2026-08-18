package com.chengukargbo.careeros.automation;

public final class AutomationEnums {
    private AutomationEnums() {}
    public enum State { NOT_APPROVED, APPROVED_FOR_PREP, NEEDS_ANSWERS, READY_FOR_REVIEW, APPROVED_TO_SUBMIT, BLOCKED }
    public enum SubmissionMode { PREPARE_ONLY, REQUIRE_REVIEW_BEFORE_SUBMIT }
    public enum AtsType { GREENHOUSE, LEVER, ASHBY, WORKDAY, ICIMS, TALEO, CUSTOM, UNKNOWN }
    public enum Source { USER, SYSTEM, AUTOMATION }
}
