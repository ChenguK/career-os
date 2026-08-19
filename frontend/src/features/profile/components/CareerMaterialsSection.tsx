import { type FormEvent, useState } from "react";
import type { CareerMaterial } from "../types/careerMaterial";
import { careerMaterialDownloadUrl, deactivateCareerMaterial, deleteCareerMaterial, setDefaultCareerMaterial, uploadCareerMaterial } from "../api/careerMaterialsApi";

export default function CareerMaterialsSection({materials,onChange}:{materials:CareerMaterial[];onChange:()=>Promise<void>}){
  const [file,setFile]=useState<File|null>(null);const [displayName,setDisplayName]=useState("");
  const [family,setFamily]=useState("");const [seniority,setSeniority]=useState("");const [version,setVersion]=useState("");
  const [notes,setNotes]=useState("");
  const [busy,setBusy]=useState(false);const [error,setError]=useState("");const [message,setMessage]=useState("");
  async function upload(event:FormEvent){event.preventDefault();if(!file)return;setBusy(true);setError("");setMessage("");
    try{await uploadCareerMaterial(file,{displayName,targetJobFamily:family,targetSeniority:seniority,versionLabel:version,notes});await onChange();setFile(null);setDisplayName("");setFamily("");setSeniority("");setVersion("");setNotes("");setMessage("Resume uploaded to CareerOS.");}
    catch(caught){setError(caught instanceof Error?caught.message:"Resume could not be uploaded.");}finally{setBusy(false);}}
  async function action(work:()=>Promise<unknown>,success:string){setBusy(true);setError("");try{await work();await onChange();setMessage(success);}catch(caught){setError(caught instanceof Error?caught.message:"Resume action failed.");}finally{setBusy(false);}}
  return <section className="career-materials-manager" aria-labelledby="career-materials-heading"><h2 id="career-materials-heading">Career Materials</h2>
    <p>Upload reusable PDF or DOCX résumés. CareerOS stores them locally and never uploads them to an ATS from this page.</p>
    <form id="upload" onSubmit={upload}><label>Resume file <input type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" onChange={e=>{const selected=e.target.files?.[0]??null;setFile(selected);if(selected&&!displayName)setDisplayName(selected.name.replace(/\.(pdf|docx)$/i,""));}} required /></label>
      <label>Display name <input value={displayName} onChange={e=>setDisplayName(e.target.value)} required maxLength={200}/></label>
      <label>Target job family <input value={family} onChange={e=>setFamily(e.target.value)} maxLength={120}/></label>
      <label>Target seniority <input value={seniority} onChange={e=>setSeniority(e.target.value)} maxLength={80}/></label>
      <label>Version label <input value={version} onChange={e=>setVersion(e.target.value)} maxLength={100}/></label>
      <label>Notes <textarea value={notes} onChange={e=>setNotes(e.target.value)} maxLength={2000}/></label>
      <button disabled={busy||!file}>{busy?"Working…":"Upload resume"}</button></form>
    {error&&<p role="alert">{error}</p>}{message&&<p role="status">{message}</p>}
    {materials.length===0?<p>No resume materials uploaded yet.</p>:<ul>{materials.map(material=><li key={material.id}>
      <strong>{material.displayName}</strong>{material.profileDefault?" — Profile default":""}{!material.active?" — Archived":""}<br/>
      {material.originalFilename} · {(material.fileSize/1024).toFixed(1)} KB
      {material.targetJobFamily&&<> · {material.targetJobFamily}</>}{material.targetSeniority&&<> · {material.targetSeniority}</>}
      <div><a href={careerMaterialDownloadUrl(material.id)}>Download</a>{material.active&&!material.profileDefault&&<button type="button" disabled={busy} onClick={()=>void action(()=>setDefaultCareerMaterial(material.id),"Default resume updated.")}>Set as default</button>}{material.active&&<button type="button" disabled={busy} onClick={()=>void action(()=>deactivateCareerMaterial(material.id),"Resume archived.")}>Archive</button>}<button type="button" disabled={busy} onClick={()=>void action(()=>deleteCareerMaterial(material.id),"Resume removed.")}>Remove</button></div>
    </li>)}</ul>}
  </section>;
}
