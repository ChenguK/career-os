import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import CompanyList from "./CompanyList";
import type { Company } from "../types/company";

const github: Company = {
  id: 1,
  name: "GitHub",
  websiteUrl: "https://github.com",
  careersUrl: "https://www.github.careers",
  industry: "Developer Platform",
  companyType: "SaaS",
  mission: "Build a home for developers",
  products: "Code hosting and developer collaboration",
  techStack: "Ruby, Go, JavaScript",
  remotePolicy: "Remote options available",
  salaryNotes: null,
  generalNotes: "Dream company playbook",
  dreamCompany: true,
  createdAt: "2026-08-04T11:10:46Z",
  updatedAt: "2026-08-04T12:10:59Z",
};

describe("CompanyList", () => {
  it("displays company information", () => {
    render(<CompanyList companies={[github]} />);

    expect(
      screen.getByRole("heading", { name: "GitHub" }),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Developer Platform"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Dream company"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("link", { name: "Website" }),
    ).toHaveAttribute("href", "https://github.com");

    expect(
      screen.getByRole("link", { name: "Careers" }),
    ).toHaveAttribute(
      "href",
      "https://www.github.careers",
    );
  });

  it("displays an empty state", () => {
    render(<CompanyList companies={[]} />);

    expect(
      screen.getByText("No companies found."),
    ).toBeInTheDocument();
  });
});