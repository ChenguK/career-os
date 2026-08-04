import type {
  Company,
  CompanyInput,
} from "../types/company";

interface ApiError {
  status?: number;
  error?: string;
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

export async function getCompanies(
  search = "",
): Promise<Company[]> {
  const query = search.trim()
    ? `?search=${encodeURIComponent(search.trim())}`
    : "";

  const response = await fetch(`/api/companies${query}`);

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json() as Promise<Company[]>;
}

export async function createCompany(
  input: CompanyInput,
): Promise<Company> {
  const response = await fetch("/api/companies", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json() as Promise<Company>;
}