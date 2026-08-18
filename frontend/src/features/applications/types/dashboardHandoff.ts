export const dashboardHandoffActions = [
  "APPLY",
  "FINISH_APPLICATION",
  "FOLLOW_UP",
  "PREPARE_INTERVIEW",
] as const;

export type DashboardHandoffAction =
  (typeof dashboardHandoffActions)[number];

export interface DashboardHandoff {
  action: DashboardHandoffAction;
  jobOpportunityId: number;
  applicationId?: number;
}

export interface DashboardHandoffLocationState {
  dashboardHandoff: DashboardHandoff;
}

export function dashboardHandoffState(
  handoff: DashboardHandoff,
): DashboardHandoffLocationState {
  return { dashboardHandoff: handoff };
}

export function readDashboardHandoff(
  state: unknown,
): DashboardHandoff | null {
  if (typeof state !== "object" || state === null) {
    return null;
  }

  const candidate = (state as Record<string, unknown>)
    .dashboardHandoff;
  if (typeof candidate !== "object" || candidate === null) {
    return null;
  }

  const values = candidate as Record<string, unknown>;
  const action = values.action;
  const jobOpportunityId = values.jobOpportunityId;
  const applicationId = values.applicationId;

  if (
    typeof action !== "string" ||
    !dashboardHandoffActions.includes(
      action as DashboardHandoffAction,
    ) ||
    !isPositiveInteger(jobOpportunityId) ||
    (applicationId !== undefined && !isPositiveInteger(applicationId))
  ) {
    return null;
  }

  return {
    action: action as DashboardHandoffAction,
    jobOpportunityId,
    ...(applicationId === undefined ? {} : { applicationId }),
  };
}

function isPositiveInteger(value: unknown): value is number {
  return typeof value === "number" &&
    Number.isInteger(value) && value > 0;
}
