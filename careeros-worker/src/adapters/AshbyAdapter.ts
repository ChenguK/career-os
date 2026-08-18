import type { Page } from "playwright";

import type { ApprovedFieldPlanItem, FieldPreparationResult, FormIdentity, ObservedQuestion } from "../domain.js";
import type { AtsAdapter } from "./AtsAdapter.js";

export class AshbyAdapter implements AtsAdapter {
  readonly provider = "ASHBY";

  canHandle(url: URL): boolean {
    return url.protocol === "https:" && url.hostname === "jobs.ashbyhq.com";
  }

  async inspectIdentity(page: Page): Promise<FormIdentity> {
    const url = new URL(page.url());
    if (!this.canHandle(url)) {
      throw new Error("Ashby adapter cannot inspect this URL");
    }
    url.hash = "";
    const segments = url.pathname.split("/").filter(Boolean);
    const requisitionId = segments.at(-1) ?? null;
    return {
      normalizedFormUrl: url.toString(),
      externalRequisitionId: requisitionId,
      externalFormKey: segments.length >= 2
        ? `ashby:${segments.slice(-2).join(":")}` : null,
    };
  }

  async collectQuestions(page: Page): Promise<ObservedQuestion[]> {
    void page;
    // Increment 18 establishes the adapter contract only. A later, explicitly
    // authorized inspection increment will provide the DOM collector.
    return [];
  }

  async prepareFields(page: Page, fields: ApprovedFieldPlanItem[]): Promise<FieldPreparationResult[]> {
    const results: FieldPreparationResult[] = [];
    for (const field of fields) {
      // Exact canonical field identifiers are the only allowed mapping. The
      // adapter never guesses from labels or surrounding page content.
      const selector = `[name="${field.canonicalKey}"], [data-field-key="${field.canonicalKey}"]`;
      const locator = page.locator(selector);
      if (await locator.count() !== 1) {
        results.push(result(field.id, "SKIPPED", "No unambiguous supported field was found"));
        continue;
      }
      try {
        if (field.answerType === "BOOLEAN") {
          if (field.booleanValue) await locator.check(); else await locator.uncheck();
        } else {
          const value = field.answerType === "NUMBER"
            ? String(field.numberValue) : field.textValue ?? "";
          await locator.fill(value);
        }
        results.push(result(field.id, "PREPARED", null, new Date().toISOString()));
      } catch {
        results.push(result(field.id, "FAILED", "The approved field could not be prepared"));
      }
    }
    return results;
  }
}

function result(planItemId: number, outcome: FieldPreparationResult["outcome"],
  safeMessage: string | null, preparedAt: string | null = null): FieldPreparationResult {
  return { planItemId, outcome, safeMessage, preparedAt };
}
