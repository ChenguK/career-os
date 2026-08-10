import { afterEach, describe, expect, it, vi } from "vitest";

import {
  getApplicationTracker,
  getImportBatch,
  getImportHistory,
  persistCsvImport,
  previewCsvImport,
} from "./applicationsApi";

describe("getApplicationTracker", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("encodes the canonical server query including repeated filters", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        content: [],
        page: 1,
        size: 25,
        totalRows: 0,
        totalPages: 0,
      }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await getApplicationTracker({
      search: " platform ",
      statuses: ["SAVED", "APPLIED"],
      priorities: [1, 2],
      remoteTypes: ["REMOTE", "HYBRID"],
      companyId: 9,
      applicationDateFrom: "2026-08-01",
      applicationDateTo: "2026-08-31",
      datePostedFrom: "2026-07-01",
      datePostedTo: "2026-07-31",
      followUpDateFrom: "2026-09-01",
      followUpDateTo: "2026-09-30",
      sort: "company",
      direction: "desc",
      page: 1,
      size: 25,
    });

    const requestedUrl = String(fetchMock.mock.calls[0][0]);
    const parameters = new URL(
      requestedUrl,
      "http://localhost",
    ).searchParams;

    expect(parameters.get("search")).toBe("platform");
    expect(parameters.getAll("statuses")).toEqual([
      "SAVED",
      "APPLIED",
    ]);
    expect(parameters.getAll("priorities")).toEqual(["1", "2"]);
    expect(parameters.getAll("remoteTypes")).toEqual([
      "REMOTE",
      "HYBRID",
    ]);
    expect(parameters.get("companyId")).toBe("9");
    expect(parameters.get("applicationDateFrom"))
      .toBe("2026-08-01");
    expect(parameters.get("applicationDateTo"))
      .toBe("2026-08-31");
    expect(parameters.get("datePostedFrom")).toBe("2026-07-01");
    expect(parameters.get("datePostedTo")).toBe("2026-07-31");
    expect(parameters.get("followUpDateFrom"))
      .toBe("2026-09-01");
    expect(parameters.get("followUpDateTo"))
      .toBe("2026-09-30");
    expect(parameters.get("sort")).toBe("company");
    expect(parameters.get("direction")).toBe("desc");
    expect(parameters.get("page")).toBe("1");
    expect(parameters.get("size")).toBe("25");
  });
});

describe("previewCsvImport", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("sends the selected file as multipart form data", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ rows: [] }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const file = new File(["Job Title\nEngineer"], "jobs.csv", {
      type: "text/csv",
    });

    await previewCsvImport(file);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/applications/import/preview");
    expect(options.method).toBe("POST");
    expect(options.body).toBeInstanceOf(FormData);
    expect((options.body as FormData).get("file")).toBe(file);
    expect(options.headers).toBeUndefined();
  });
});

describe("persistCsvImport", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("sends selected canonical rows in one bulk request", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ batchId: 1, rows: [] }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const request = {
      filename: "jobs.csv",
      totalRows: 4,
      rows: [{
        rowNumber: 2,
        fields: { position_title: "Engineer" },
      }],
    };

    await persistCsvImport(request);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/applications/import",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify(request),
      }),
    );
  });
});

describe("import history", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("requests one bounded history page and one selected batch page", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ content: [] }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await getImportHistory(2, 25);
    await getImportBatch(9, 1, 25);

    expect(fetchMock.mock.calls[0][0])
      .toBe("/api/applications/imports?page=2&size=25");
    expect(fetchMock.mock.calls[1][0])
      .toBe("/api/applications/imports/9?page=1&size=25");
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
