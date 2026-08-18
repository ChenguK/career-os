import type { Page } from "playwright";

import type { ApprovedFieldPlanItem, FieldPreparationResult, FormIdentity, ObservedQuestion } from "../domain.js";

export interface AtsAdapter {
  readonly provider: string;
  canHandle(url: URL): boolean;
  inspectIdentity(page: Page): Promise<FormIdentity>;
  collectQuestions(page: Page): Promise<ObservedQuestion[]>;
  prepareFields(page: Page, fields: ApprovedFieldPlanItem[]): Promise<FieldPreparationResult[]>;
}
