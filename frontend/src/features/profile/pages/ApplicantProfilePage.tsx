import { useEffect, useMemo, useState } from "react";

import {
  getApplicantProfile,
  saveApplicantProfile,
  verifyApplicantProfile,
} from "../api/applicantProfileApi";
import ApplicantProfileForm from "../components/ApplicantProfileForm";
import type {
  ApplicantProfile,
  ApplicantProfileInput,
} from "../types/applicantProfile";
import { getCareerMaterials } from "../api/careerMaterialsApi";
import type { CareerMaterial } from "../types/careerMaterial";
import CareerMaterialsSummary from "../components/CareerMaterialsSummary";
import {
  emptyApplicantProfileInput,
  profileToInput,
} from "../types/applicantProfile";

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export default function ApplicantProfilePage() {
  const [profile, setProfile] = useState<ApplicantProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isVerifying, setIsVerifying] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [materials,setMaterials]=useState<CareerMaterial[]>([]);

  useEffect(() => {
    let isCancelled = false;
    async function load() {
      try {
        const result = await getApplicantProfile();
        if (!isCancelled) {
          setProfile(result);
          if(result.exists) setMaterials(await getCareerMaterials());
        }
      } catch (caughtError) {
        if (!isCancelled) {
          setError(caughtError instanceof Error
            ? caughtError.message
            : "Applicant profile could not be loaded.");
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }
    void load();
    return () => { isCancelled = true; };
  }, []);

  const initialValues = useMemo(
    () => profile?.exists
      ? profileToInput(profile)
      : emptyApplicantProfileInput,
    [profile],
  );

  async function save(input: ApplicantProfileInput) {
    setError("");
    setMessage("");
    const saved = await saveApplicantProfile(input);
    setProfile(saved);
    setMessage(saved.verified
      ? "Applicant profile saved. Existing verification remains current."
      : "Applicant profile saved. Verify it before future autofill use.");
    return saved;
  }

  async function verify() {
    setIsVerifying(true);
    setError("");
    setMessage("");
    try {
      const verified = await verifyApplicantProfile();
      setProfile(verified);
      setMessage("Applicant profile verified.");
    } catch (caughtError) {
      setError(caughtError instanceof Error
        ? caughtError.message
        : "Applicant profile could not be verified.");
    } finally {
      setIsVerifying(false);
    }
  }

  if (isLoading) {
    return <main className="applicant-profile-page">
      <p>Loading applicant profile...</p>
    </main>;
  }

  return (
    <main className="applicant-profile-page">
      <header>
        <p>Career OS</p>
        <h1>Applicant Profile</h1>
        <p>
          Maintain the information CareerOS may use as verified application
          defaults. Profile changes do not alter existing applications.
        </p>
      </header>

      {error && <p role="alert">{error}</p>}

      {!profile?.exists && !error && (
        <p>No applicant profile has been saved yet.</p>
      )}

      {profile?.exists && (
        <section className="profile-verification"
          aria-labelledby="profile-verification-heading">
          <h2 id="profile-verification-heading">Verification</h2>
          {profile.verified && profile.lastVerifiedAt ? (
            <p role="status">
              Verified {formatDateTime(profile.lastVerifiedAt)}
            </p>
          ) : (
            <p>
              Not verified. Review every value before marking this profile
              safe for future autofill.
            </p>
          )}
          <button type="button" onClick={() => void verify()}
            disabled={isVerifying}>
            {isVerifying ? "Verifying…" : "Verify current profile"}
          </button>
        </section>
      )}

      {!error && (
        <ApplicantProfileForm
          key={`${profile?.id ?? "new"}-${profile?.updatedAt ?? "empty"}`}
          initialValues={initialValues}
          onSubmit={save}
        />
      )}

      {profile?.exists && <CareerMaterialsSummary materials={materials} />}

      {message && <p className="status-message" role="status">{message}</p>}
    </main>
  );
}
