export interface ApiErrorResponse {
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  timestamp?: string;
}

async function getErrorMessage(
  response: Response,
): Promise<string> {
  try {
    const body =
      (await response.json()) as ApiErrorResponse;

    return body.message ?? "The request could not be completed.";
  } catch {
    return "The request could not be completed.";
  }
}

export async function apiRequest<T>(
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(input, init);

  if (!response.ok) {
    throw new Error(await getErrorMessage(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}