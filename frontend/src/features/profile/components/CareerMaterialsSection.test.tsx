import {fireEvent,render,screen,waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {describe,it,expect,vi,beforeEach} from "vitest";
import CareerMaterialsSection from "./CareerMaterialsSection";
import {uploadCareerMaterial,setDefaultCareerMaterial,deactivateCareerMaterial,deleteCareerMaterial} from "../api/careerMaterialsApi";

vi.mock("../api/careerMaterialsApi",()=>({uploadCareerMaterial:vi.fn(),setDefaultCareerMaterial:vi.fn(),deactivateCareerMaterial:vi.fn(),deleteCareerMaterial:vi.fn(),careerMaterialDownloadUrl:(id:number)=>`/api/applicant-profile/materials/${id}/download`}));
const material={id:4,applicantProfileId:1,materialType:"RESUME" as const,displayName:"Operations Resume",originalFilename:"paid-resume.pdf",mimeType:"application/pdf",fileSize:2048,active:true,notes:null,targetJobFamily:"Operations",targetSeniority:"MID",versionLabel:"2026",profileDefault:false,createdAt:"2026-01-01T00:00:00Z",updatedAt:"2026-01-01T00:00:00Z"};

describe("CareerMaterialsSection",()=>{
 beforeEach(()=>vi.clearAllMocks());
 it("lists resume metadata and supports default and archive actions",async()=>{
  vi.mocked(setDefaultCareerMaterial).mockResolvedValue({...material,profileDefault:true});vi.mocked(deactivateCareerMaterial).mockResolvedValue({...material,active:false});
  const onChange=vi.fn().mockResolvedValue(undefined);const user=userEvent.setup();render(<CareerMaterialsSection materials={[material]} onChange={onChange}/>);
  expect(screen.getByText("Operations Resume")).toBeInTheDocument();expect(screen.getByRole("link",{name:"Download"})).toHaveAttribute("href","/api/applicant-profile/materials/4/download");
  await user.click(screen.getByRole("button",{name:"Set as default"}));expect(setDefaultCareerMaterial).toHaveBeenCalledWith(4);
  await user.click(screen.getByRole("button",{name:"Archive"}));expect(deactivateCareerMaterial).toHaveBeenCalledWith(4);
 });
 it("shows archived materials and delegates safe removal to the backend",async()=>{
  vi.mocked(deleteCareerMaterial).mockResolvedValue(undefined);const onChange=vi.fn().mockResolvedValue(undefined);const user=userEvent.setup();
  render(<CareerMaterialsSection materials={[{...material,active:false}]} onChange={onChange}/>);
  expect(screen.getByText(/Archived/)).toBeInTheDocument();
  await user.click(screen.getByRole("button",{name:"Remove"}));expect(deleteCareerMaterial).toHaveBeenCalledWith(4);expect(onChange).toHaveBeenCalled();
 });
 it("uploads only a chosen local resume to CareerOS",async()=>{
  vi.mocked(uploadCareerMaterial).mockResolvedValue(material);const onChange=vi.fn().mockResolvedValue(undefined);const user=userEvent.setup();render(<CareerMaterialsSection materials={[]} onChange={onChange}/>);
  const file=new File(["%PDF-1.7"],"resume.pdf",{type:"application/pdf"});await user.upload(screen.getByLabelText("Resume file"),file);expect(screen.getByLabelText("Display name")).toHaveValue("resume");
  fireEvent.submit(screen.getByRole("button",{name:"Upload resume"}).closest("form")!);
  await waitFor(()=>expect(uploadCareerMaterial).toHaveBeenCalledWith(file,expect.objectContaining({displayName:"resume"})));
  expect(await screen.findByText("Resume uploaded to CareerOS.")).toBeInTheDocument();
 });
});
