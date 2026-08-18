import { afterEach, describe, expect, it, vi } from "vitest";

import {
  getApplicationTracker,
  exportApplicationTrackerCsv,
  exportApplicationTrackerXlsx,
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

describe("exportApplicationTrackerCsv", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("sends current criteria without pagination and reads the filename", async () => {
    const blob = new Blob(["job_id\r\n1\r\n"], { type: "text/csv" });
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers({
        "Content-Disposition": "attachment; filename=\"current.csv\"",
      }),
      blob: async () => blob,
    });
    vi.stubGlobal("fetch", fetchMock);

    const result = await exportApplicationTrackerCsv("CURRENT_VIEW", {
      search: " platform ", statuses: ["APPLIED"], priorities: [1],
      remoteTypes: ["HYBRID"], companyId: 9, sort: "company",
      direction: "desc", page: 4, size: 25,
    });

    const url = new URL(String(fetchMock.mock.calls[0][0]), "http://localhost");
    expect(url.searchParams.get("mode")).toBe("CURRENT_VIEW");
    expect(url.searchParams.get("search")).toBe("platform");
    expect(url.searchParams.getAll("statuses")).toEqual(["APPLIED"]);
    expect(url.searchParams.getAll("priorities")).toEqual(["1"]);
    expect(url.searchParams.getAll("remoteTypes")).toEqual(["HYBRID"]);
    expect(url.searchParams.get("companyId")).toBe("9");
    expect(url.searchParams.get("sort")).toBe("company");
    expect(url.searchParams.get("direction")).toBe("desc");
    expect(url.searchParams.has("page")).toBe(false);
    expect(url.searchParams.has("size")).toBe(false);
    expect(result).toEqual({ blob, filename: "current.csv" });
  });

  it("ignores tracker criteria for all applications", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers(),
      blob: async () => new Blob(),
    });
    vi.stubGlobal("fetch", fetchMock);

    await exportApplicationTrackerCsv("ALL", {
      search: "hidden", statuses: ["OFFER"], page: 7,
    });

    expect(fetchMock.mock.calls[0][0])
      .toBe("/api/applications/tracker/export.csv?mode=ALL");
  });
});

describe("exportApplicationTrackerXlsx", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("sends current-view criteria without pagination", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers({
        "Content-Disposition": "attachment; filename=careeros-applications-current-view-2026-08-13.xlsx",
      }),
      blob: async () => new Blob(["xlsx"]),
    });
    vi.stubGlobal("fetch", fetchMock);

    const result = await exportApplicationTrackerXlsx("CURRENT_VIEW", {
      search: "engineer",
      statuses: ["APPLIED"],
      sort: "company",
      direction: "desc",
      page: 8,
      size: 1,
    });

    const url = String(fetchMock.mock.calls[0][0]);
    expect(url).toContain("/api/applications/tracker/export.xlsx?");
    expect(url).toContain("search=engineer");
    expect(url).toContain("statuses=APPLIED");
    expect(url).toContain("sort=company");
    expect(url).not.toContain("page=");
    expect(url).not.toContain("size=");
    expect(result.filename).toBe(
      "careeros-applications-current-view-2026-08-13.xlsx",
    );
  });

  it("ignores tracker criteria for all applications", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers(),
      blob: async () => new Blob(["xlsx"]),
    });
    vi.stubGlobal("fetch", fetchMock);
    await exportApplicationTrackerXlsx("ALL", { search: "ignored", page: 4 });
    expect(String(fetchMock.mock.calls[0][0]))
      .toBe("/api/applications/tracker/export.xlsx?mode=ALL");
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
