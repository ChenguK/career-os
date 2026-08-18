import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";

import AppLayout from "./AppLayout";

describe("AppLayout", () => {
  it("includes Applicant Profile in primary navigation", () => {
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <AppLayout />
      </MemoryRouter>,
    );

    const link = screen.getByRole("link", { name: "Applicant Profile" });
    expect(link).toHaveAttribute("href", "/profile");
    expect(link).toHaveClass("active");
    expect(screen.getByRole("link", { name: "Approved Answers" }))
      .toHaveAttribute("href", "/approved-answers");
    expect(screen.getByRole("link", { name: "Questions" }))
      .toHaveAttribute("href", "/questions");
  });
});
