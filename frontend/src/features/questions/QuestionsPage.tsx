import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getApplications, getApplicationAutomation,
  type ApplicationAutomation } from "../applications/api/applicationsApi";
import type { Application } from "../applications/types/application";
import { getApprovedAnswers } from "../answers/api/approvedAnswersApi";
import type { ApprovedAnswer } from "../answers/types/approvedAnswer";
import { addTemplates, answerQuestion, createManualQuestion, getQuestions,
  getTemplates, linkApprovedAnswer, questionAction, type Question,
  type Template } from "./questionsApi";

const humanize = (value: string) => value.toLowerCase().replaceAll("_", " ")
  .replace(/\b\w/g, (character) => character.toUpperCase());

export default function QuestionsPage() {
  const [params] = useSearchParams();
  const focusedId = Number(params.get("applicationId")) || undefined;
  const [targetId, setTargetId] = useState<number | null>(focusedId ?? null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [answers, setAnswers] = useState<ApprovedAnswer[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [templates, setTemplates] = useState<Template[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [family, setFamily] = useState("SOFTWARE_ENGINEER");
  const [seniority, setSeniority] = useState("");
  const [manualOpen, setManualOpen] = useState(false);
  const [error, setError] = useState("");
  const [automation, setAutomation] = useState<Record<number, ApplicationAutomation>>({});

  useEffect(() => {
    let cancelled = false;
    Promise.all([getQuestions(focusedId), getApplications(), getApprovedAnswers()])
      .then(([loadedQuestions, loadedApplications, loadedAnswers]) => {
        if (cancelled) return;
        setQuestions(loadedQuestions); setApplications(loadedApplications);
        setAnswers(loadedAnswers);
        void Promise.all(loadedApplications.map((application) =>
          getApplicationAutomation(application.id))).then((values) => {
            if (!cancelled) setAutomation(Object.fromEntries(values.map((value) =>
              [value.applicationId, value])));
          });
      }).catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "Question Queue could not be loaded.");
      });
    return () => { cancelled = true; };
  }, [focusedId]);

  const reload = () => getQuestions(focusedId).then(setQuestions);
  const replace = (updated: Question) => setQuestions((current) =>
    current.map((question) => question.id === updated.id ? updated : question));
  const groups = questions.reduce((map, question) => {
    const group = map.get(question.applicationId) ?? []; group.push(question);
    map.set(question.applicationId, group); return map;
  }, new Map<number, Question[]>());

  async function preview() {
    try { const loaded = await getTemplates(family, seniority); setTemplates(loaded);
      setSelected(loaded.map((template) => template.id)); }
    catch (caught) { setError(caught instanceof Error ? caught.message : "Templates could not be loaded."); }
  }

  return <main className="questions-page">
    <header><p>Career OS</p><h1>Application Questions</h1>
      <p>Review preparation questions. Nothing here submits an application.</p></header>
    {error && <p role="alert">{error}</p>}
    <label>Application <select value={targetId ?? ""}
      onChange={(event) => setTargetId(Number(event.target.value) || null)}>
      <option value="">Choose an application</option>{applications.map((application) =>
        <option key={application.id} value={application.id}>{application.companyName ?? "No company"} — {application.positionTitle}</option>)}
    </select></label>

    <section className="likely-questions"><h2>Add Likely Questions</h2>
      <label>Job family <select value={family} onChange={(event) => setFamily(event.target.value)}>
        <option value="SOFTWARE_ENGINEER">Software Engineer</option><option value="FULL_STACK_ENGINEER">Full Stack Engineer</option>
        <option value="FRONTEND_ENGINEER">Frontend Engineer</option><option value="BACKEND_ENGINEER">Backend Engineer</option>
      </select></label>
      <label>Seniority <select value={seniority} onChange={(event) => setSeniority(event.target.value)}>
        <option value="">Any</option><option value="ENTRY_LEVEL">Entry Level</option><option value="MID_LEVEL">Mid Level</option><option value="SENIOR">Senior</option>
      </select></label>
      <button type="button" onClick={() => void preview()}>Preview templates</button>
      {templates.length > 0 && <button type="button" onClick={() => setSelected(
        selected.length === templates.length ? [] : templates.map((template) => template.id))}>
        {selected.length === templates.length ? "Clear selection" : "Select all"}</button>}
      {templates.map((template) => <label key={template.id}><input type="checkbox"
        checked={selected.includes(template.id)} onChange={(event) => setSelected((current) =>
          event.target.checked ? [...current, template.id] : current.filter((id) => id !== template.id))} />
        {template.representativeQuestion}</label>)}
      {templates.length > 0 && <button type="button" disabled={!targetId || selected.length === 0}
        onClick={() => targetId && void addTemplates(targetId, selected).then(reload)}>
        Add selected questions</button>}
    </section>

    <button type="button" disabled={!targetId} onClick={() => setManualOpen((open) => !open)}>Add manual question</button>
    {manualOpen && targetId && <ManualForm applicationId={targetId}
      onCreated={(question) => { setQuestions((current) => [...current, question]); setManualOpen(false); }} />}

    {questions.length === 0 ? <p>No application questions yet.</p> : [...groups.entries()].map(([id, items]) =>
      <section className="question-group" key={id}>
        <h2>{items[0].companyName ?? "No company"} — {items[0].positionTitle}</h2>
        <p>{humanize(items[0].lifecycleStatus)} · {items.filter((q) => q.status === "UNANSWERED").length} unanswered · {items.filter((q) => q.status === "NEEDS_REVIEW").length} review · {items.filter((q) => q.status === "BLOCKED").length} blockers</p>
        {automation[id] && <p>Preparation: <strong>{humanize(automation[id].state)}</strong>
          {automation[id].unresolvedRequiredCount > 0 && ` — ${automation[id].unresolvedRequiredCount} required questions need answers or approval.`}
          {automation[id].blockerCount > 0 && ` — ${automation[id].blockerCount} question blockers must be resolved.`}</p>}
        <Link to={`/applications?editJob=${items[0].jobOpportunityId}`}>Back to Application Tracker</Link>
        {items.map((question) => <QuestionCard key={question.id} question={question}
          answers={answers} onReplace={replace} />)}
      </section>)}
  </main>;
}

function ManualForm({ applicationId, onCreated }: { applicationId: number; onCreated: (question: Question) => void }) {
  const [text, setText] = useState(""); const [key, setKey] = useState("");
  return <form className="manual-question-form" onSubmit={(event) => {
    event.preventDefault(); void createManualQuestion({ applicationId,
      canonicalQuestionKey: key.trim() || null, questionText: text,
      answerType: "TEXT", required: false, classification: "UNKNOWN", notes: null,
    }).then(onCreated);
  }}><h2>Manual question</h2><label>Question text<input required value={text}
      onChange={(event) => setText(event.target.value)} /></label>
    <label>Canonical key (optional)<input value={key} onChange={(event) => setKey(event.target.value)} /></label>
    <button type="submit">Save question</button></form>;
}

function QuestionCard({ question, answers, onReplace }: { question: Question;
  answers: ApprovedAnswer[]; onReplace: (question: Question) => void }) {
  const [answer, setAnswer] = useState(question.proposedAnswer ?? "");
  const matches = answers.filter((candidate) => candidate.canonicalKey === question.canonicalQuestionKey
    && candidate.userApproved && candidate.authorityAvailable);
  const action = (name: "approve" | "block" | "unblock" | "reject-suggestion") =>
    questionAction(question.id, name).then(onReplace);
  return <article><h3>{question.questionText}{question.required ? " *" : ""}</h3>
    <p>{humanize(question.classification)} · {humanize(question.source)} · <strong>{humanize(question.status)}</strong></p>
    {question.proposedAnswer && <p>Suggested: {question.proposedAnswer}</p>}
    {question.approvedAnswer && <p>Approved: {question.approvedAnswer}</p>}
    <form onSubmit={(event) => { event.preventDefault(); void answerQuestion(question.id, answer).then(onReplace); }}>
      <label>Answer<input value={answer} onChange={(event) => setAnswer(event.target.value)} /></label>
      <button type="submit">Save answer</button></form>
    {matches.length > 0 && <label>Link exact Approved Answer<select defaultValue=""
      onChange={(event) => event.target.value && void linkApprovedAnswer(question.id, Number(event.target.value)).then(onReplace)}>
      <option value="">Choose approved answer</option>{matches.map((candidate) =>
        <option key={candidate.id} value={candidate.id}>{candidate.canonicalKey}</option>)}</select></label>}
    <button type="button" onClick={() => void action("approve")}>Approve answer</button>
    {question.status === "BLOCKED" ? <button type="button" onClick={() => void action("unblock")}>Unblock</button>
      : <button type="button" onClick={() => void action("block")}>Mark blocked</button>}
    {question.status === "NEEDS_REVIEW" && <button type="button"
      onClick={() => void action("reject-suggestion")}>Reject suggestion</button>}
  </article>;
}
