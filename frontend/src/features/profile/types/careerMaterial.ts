export interface CareerMaterial {
  id: number;
  applicantProfileId: number;
  materialType: "RESUME";
  displayName: string;
  originalFilename: string;
  mimeType: string;
  fileSize: number;
  active: boolean;
  notes: string | null;
  targetJobFamily: string | null;
  targetSeniority: string | null;
  versionLabel: string | null;
  profileDefault: boolean;
  createdAt: string;
  updatedAt: string;
}
