import { defaultDependencies, runApplication, type WorkerDependencies } from "./worker.js";
import { pathToFileURL } from "node:url";

export async function startWorker(
  environment: NodeJS.ProcessEnv = process.env,
  dependencies?: WorkerDependencies,
): Promise<"idle" | "completed"> {
  const rawApplicationId = environment.CAREEROS_APPLICATION_ID;
  if (!rawApplicationId) return "idle";
  const applicationId = Number(rawApplicationId);
  if (!Number.isSafeInteger(applicationId) || applicationId <= 0) {
    throw new Error("CAREEROS_APPLICATION_ID must be a positive integer");
  }
  const baseUrl = environment.CAREEROS_API_URL ?? "http://127.0.0.1:8080";
  await runApplication(applicationId, dependencies ?? defaultDependencies(baseUrl));
  return "completed";
}

export function isMainModule(moduleUrl: string, entryPath: string | undefined): boolean {
  return entryPath !== undefined && moduleUrl === pathToFileURL(entryPath).href;
}

if (isMainModule(import.meta.url, process.argv[1])) {
  startWorker().then((status) => {
    console.log(`CareerOS worker ${status}`);
  }).catch((error: unknown) => {
    console.error(error instanceof Error ? error.message : "CareerOS worker failed");
    process.exitCode = 1;
  });
}
