import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../types/job";

interface ApiError {
  message?: string;
}

async function parseError(response: Response): Promise<string> {
  try {
    const error = (await response.json()) as ApiError;

    return error.message ?? "The request could not be completed.";
  } catch {
    return "The request could not be completed.";
  }
}

export async function getJobs(
  search = "",
): Promise<JobOpportunity[]> {
  const query = search.trim()
    ? `?search=${encodeURIComponent(search.trim())}`
    : "";

  const response = await fetch(`/api/jobs${query}`);

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json() as Promise<JobOpportunity[]>;
}

export async function createJob(
  input: JobOpportunityInput,
): Promise<JobOpportunity> {
  const response = await fetch("/api/jobs", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json() as Promise<JobOpportunity>;
}

export async function updateJob(
  id: number,
  input: JobOpportunityInput,
): Promise<JobOpportunity> {
  const response = await fetch(`/api/jobs/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json() as Promise<JobOpportunity>;
}

export async function deleteJob(id: number): Promise<void> {
  const response = await fetch(`/api/jobs/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}