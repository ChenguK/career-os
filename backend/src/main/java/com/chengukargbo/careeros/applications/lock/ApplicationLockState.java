package com.chengukargbo.careeros.applications.lock;

public enum ApplicationLockState {
    NOT_SUBMITTED, SUBMITTED, ARCHIVED, TESTING;

    public boolean allowsLiveInteraction() {
        return this == NOT_SUBMITTED || this == TESTING;
    }
}
