import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import CompanyForm from "./CompanyForm";
import type { Company } from "../types/company";

const createdCompany: Company = {
  id: 3,
  name: "Mozilla",
  websiteUrl: null,
  careersUrl: null,
  industry: "Technology",
  companyType: null,
  mission: null,
  products: null,
  techStack: null,
  remotePolicy: null,
  salaryNotes: null,
  generalNotes: null,
  dreamCompany: true,
  createdAt: "2026-08-04T12:30:00Z",
  updatedAt: "2026-08-04T12:30:00Z",
};

describe("CompanyForm", () => {
  it("submits entered company information", async () => {
    const user = userEvent.setup();
    const onCreate = vi.fn().mockResolvedValue(createdCompany);

    render(<CompanyForm onCreate={onCreate} />);

    await user.type(
      screen.getByLabelText("Company name"),
      "Mozilla",
    );

    await user.type(
      screen.getByLabelText("Industry"),
      "Technology",
    );

    await user.click(
      screen.getByLabelText("Dream company"),
    );

    await user.click(
      screen.getByRole("button", { name: "Add company" }),
    );

    expect(onCreate).toHaveBeenCalledOnce();

    expect(onCreate).toHaveBeenCalledWith(
      expect.objectContaining({
        name: "Mozilla",
        industry: "Technology",
        dreamCompany: true,
      }),
    );
  });

  it("clears the form after successful submission", async () => {
    const user = userEvent.setup();
    const onCreate = vi.fn().mockResolvedValue(createdCompany);

    render(<CompanyForm onCreate={onCreate} />);

    const nameInput = screen.getByLabelText("Company name");

    await user.type(nameInput, "Mozilla");

    await user.click(
      screen.getByRole("button", { name: "Add company" }),
    );

    expect(nameInput).toHaveValue("");
  });
});