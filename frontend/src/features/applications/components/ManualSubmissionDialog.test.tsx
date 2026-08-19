import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import ManualSubmissionDialog from "./ManualSubmissionDialog";
import type { ApplicationTrackerRow } from "../types/applicationTracker";

const row = {
  applicationId: 9,
  jobOpportunityId: 4,
  companyName: "SimpleClosure",
  positionTitle: "Full Stack Engineer",
} as ApplicationTrackerRow;

describe("ManualSubmissionDialog", () => {
  it("defaults to the local calendar date and submits an edited date", async () => {
    const onConfirm = vi.fn().mockResolvedValue(undefined);
    render(<ManualSubmissionDialog row={row} onCancel={vi.fn()} onConfirm={onConfirm} />);
    const date = screen.getByLabelText("Date applied") as HTMLInputElement;
    const now = new Date();
    const expected = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
    expect(date.value).toBe(expected);
    await userEvent.clear(date);
    await userEvent.type(date, "2026-08-10");
    await userEvent.click(screen.getByRole("button", { name: "Mark as Applied" }));
    expect(onConfirm).toHaveBeenCalledWith("2026-08-10");
  });

  it("cancels without recording submission", async () => {
    const onCancel = vi.fn();
    const onConfirm = vi.fn();
    render(<ManualSubmissionDialog row={row} onCancel={onCancel} onConfirm={onConfirm} />);
    await userEvent.click(screen.getByRole("button", { name: "Cancel" }));
    expect(onCancel).toHaveBeenCalled();
    expect(onConfirm).not.toHaveBeenCalled();
  });

  it("keeps the dialog open and shows backend errors", async () => {
    render(<ManualSubmissionDialog row={row} onCancel={vi.fn()}
      onConfirm={vi.fn().mockRejectedValue(new Error("Application date cannot be in the future"))} />);
    await userEvent.click(screen.getByRole("button", { name: "Mark as Applied" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("cannot be in the future");
  });
});
