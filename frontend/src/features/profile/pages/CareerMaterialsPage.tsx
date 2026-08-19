import { useEffect, useState } from "react";

import { getCareerMaterials } from "../api/careerMaterialsApi";
import CareerMaterialsSection from "../components/CareerMaterialsSection";
import type { CareerMaterial } from "../types/careerMaterial";

export default function CareerMaterialsPage() {
  const [materials, setMaterials] = useState<CareerMaterial[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadMaterials() {
    try {
      setError("");
      setMaterials(await getCareerMaterials());
    } catch (caughtError) {
      setError(caughtError instanceof Error
        ? caughtError.message
        : "Career materials could not be loaded.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    let isCancelled = false;
    async function load() {
      try {
        const result = await getCareerMaterials();
        if (!isCancelled) setMaterials(result);
      } catch (caughtError) {
        if (!isCancelled) {
          setError(caughtError instanceof Error
            ? caughtError.message
            : "Career materials could not be loaded.");
        }
      } finally {
        if (!isCancelled) setIsLoading(false);
      }
    }
    void load();
    return () => { isCancelled = true; };
  }, []);

  return (
    <main className="career-materials-page">
      <header>
        <p>Applicant Profile</p>
        <h1>Career Materials</h1>
        <p>
          Manage reusable materials owned by your applicant profile. Resume files
          remain in CareerOS and are not uploaded to an ATS from this page.
        </p>
      </header>

      {isLoading ? <p>Loading career materials...</p> : (
        <>
          {error && <p role="alert">{error}</p>}
          <CareerMaterialsSection materials={materials} onChange={loadMaterials} />
        </>
      )}
    </main>
  );
}
