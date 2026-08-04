export interface Company {
  id: number;
  name: string;
  websiteUrl: string | null;
  careersUrl: string | null;
  industry: string | null;
  companyType: string | null;
  mission: string | null;
  products: string | null;
  techStack: string | null;
  remotePolicy: string | null;
  salaryNotes: string | null;
  generalNotes: string | null;
  dreamCompany: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyInput {
  name: string;
  websiteUrl: string;
  careersUrl: string;
  industry: string;
  companyType: string;
  mission: string;
  products: string;
  techStack: string;
  remotePolicy: string;
  salaryNotes: string;
  generalNotes: string;
  dreamCompany: boolean;
}