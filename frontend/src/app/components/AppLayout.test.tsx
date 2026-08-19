import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";

import AppLayout from "./AppLayout";

describe("AppLayout", () => {
  it("exposes Profile and Materials through an accessible dropdown", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <AppLayout />
      </MemoryRouter>,
    );

    const trigger = screen.getByRole("button", { name: "Applicant Profile" });
    expect(trigger).toHaveAttribute("aria-haspopup", "menu");
    expect(trigger).toHaveAttribute("aria-expanded", "false");
    await user.click(trigger);
    expect(trigger).toHaveAttribute("aria-expanded", "true");
    expect(screen.getByRole("menuitem", { name: "Profile" })).toHaveAttribute("href", "/profile");
    expect(screen.getByRole("menuitem", { name: "Materials" })).toHaveAttribute("href", "/materials");
    fireEvent.keyDown(trigger, { key: "Escape" });
    expect(trigger).toHaveAttribute("aria-expanded", "false");
    expect(screen.getByRole("link", { name: "Approved Answers" }))
      .toHaveAttribute("href", "/approved-answers");
    expect(screen.getByRole("link", { name: "Questions" }))
      .toHaveAttribute("href", "/questions");
  });

  it("opens for hover and keyboard focus without making hover mandatory", async () => {
    const user = userEvent.setup();
    render(<MemoryRouter><AppLayout /></MemoryRouter>);
    const trigger = screen.getByRole("button", { name: "Applicant Profile" });
    await user.hover(trigger);
    expect(screen.getByRole("menuitem", { name: "Materials" })).toBeVisible();
    await user.unhover(trigger);
    fireEvent.blur(trigger);
    fireEvent.focus(trigger);
    expect(screen.getByRole("menuitem", { name: "Profile" })).toBeVisible();
  });
});
