import { Link } from "react-router-dom";

import { PROFILE_MATERIAL_DISPLAY_LIMIT } from "../constants/careerMaterials";
import type { CareerMaterial } from "../types/careerMaterial";

export default function CareerMaterialsSummary({
  materials,
}: {
  materials: CareerMaterial[];
}) {
  const displayedMaterials = materials.slice(0, PROFILE_MATERIAL_DISPLAY_LIMIT);
  const isCrowded = materials.length > PROFILE_MATERIAL_DISPLAY_LIMIT;

  return (
    <section className="career-materials-summary" aria-labelledby="career-materials-summary-heading">
      <div className="career-materials-summary__heading">
        <div>
          <h2 id="career-materials-summary-heading">Career Materials</h2>
          <p>{materials.length} Career {materials.length === 1 ? "Material" : "Materials"}</p>
        </div>
        <div className="career-materials-summary__actions">
          <Link to="/materials#upload">Upload Resume</Link>
          <Link to="/materials">Manage Materials</Link>
        </div>
      </div>

      {materials.length === 0 ? (
        <p>No career materials uploaded yet.</p>
      ) : (
        <ul className="career-materials-summary__list">
          {displayedMaterials.map((material) => (
            <li key={material.id}>
              <strong>{material.displayName}</strong>
              <span>{material.mimeType === "application/pdf" ? "PDF" : "DOCX"}</span>
              {material.targetJobFamily && <span>{material.targetJobFamily}</span>}
              {material.profileDefault && <span>Profile default</span>}
              {!material.active && <span>Archived</span>}
            </li>
          ))}
        </ul>
      )}

      {isCrowded && (
        <p>
          Showing {PROFILE_MATERIAL_DISPLAY_LIMIT} of {materials.length}.{" "}
          <Link to="/materials">View all materials</Link>
        </p>
      )}
    </section>
  );
}
