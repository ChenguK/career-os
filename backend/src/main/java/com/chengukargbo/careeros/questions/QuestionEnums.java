package com.chengukargbo.careeros.questions;

public final class QuestionEnums { private QuestionEnums() {}
 public enum AnswerType { TEXT, BOOLEAN, NUMBER, SINGLE_SELECT, MULTI_SELECT }
 public enum Classification { VERIFIED_REUSABLE, CONTEXTUAL, SENSITIVE, UNKNOWN, KNOCKOUT }
 public enum Status { UNANSWERED, NEEDS_REVIEW, ANSWERED, APPROVED, BLOCKED }
 public enum Source { MANUAL, TEMPLATE, ATS }
 public enum JobFamily { SOFTWARE_ENGINEER, FULL_STACK_ENGINEER, FRONTEND_ENGINEER, BACKEND_ENGINEER }
 public enum Seniority { ENTRY_LEVEL, MID_LEVEL, SENIOR }
}
