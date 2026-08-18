import type { AtsAdapter } from "./adapters/AtsAdapter.js";
import { AshbyAdapter } from "./adapters/AshbyAdapter.js";
import { CareerOsClient } from "./backend/CareerOsClient.js";
import { chromium, type Browser } from "playwright";

export interface WorkerDependencies {
  client: CareerOsClient;
  adapters: AtsAdapter[];
  launchBrowser?: () => Promise<Browser>;
}

export async function runApplication(applicationId: number,
  dependencies: WorkerDependencies): Promise<string> {
  const preparation = await dependencies.client.getPreparation(applicationId);
  if (!preparation.session) {
    throw new Error("Application does not have a preparation session");
  }
  const adapter = dependencies.adapters.find((candidate) =>
    candidate.canHandle(new URL(preparation.session!.normalizedFormUrl)));
  if (!adapter) throw new Error("No supported ATS adapter found for application URL");
  if (preparation.session.state === "PREPARING_FIELDS") {
    const plan = await dependencies.client.createFieldPlan(applicationId, preparation.session.id);
    let browser: Browser | undefined;
    try {
      browser = await (dependencies.launchBrowser ?? (() => chromium.launch()))();
      const page = await browser.newPage();
      await page.goto(preparation.session.normalizedFormUrl);
      if (!adapter.canHandle(new URL(page.url()))) {
        throw new Error("Application form redirected outside the approved ATS");
      }
      const results = await adapter.prepareFields(page, plan.fields);
      await dependencies.client.recordFieldResults(applicationId, preparation.session.id, results);
    } catch {
      await dependencies.client.markFailed(applicationId, preparation.session.id,
        "Approved field preparation could not be completed", true);
      throw new Error("Approved field preparation failed");
    } finally {
      await browser?.close();
    }
  } else if (preparation.session.state !== "INITIALIZED") {
    throw new Error("Preparation must be explicitly resumed before worker execution");
  }
  return adapter.provider;
}

export function defaultDependencies(baseUrl: string): WorkerDependencies {
  return {
    client: new CareerOsClient(baseUrl),
    adapters: [new AshbyAdapter()],
  };
}
