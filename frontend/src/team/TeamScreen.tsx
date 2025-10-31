import React, { useEffect, useState } from "react";
import { AxiosHeaders, AxiosResponse } from "axios";
import validator from "validator";
import AuthService from "../authentication/AuthService";
import "./TeamScreen.css";

interface TeamMemberData {
  id: string;
  email: string;
}

interface TeamMemberProps {
  email: string;
  members: TeamMemberData[];
  setMembers: React.Dispatch<React.SetStateAction<TeamMemberData[]>>;
  setMemberRemovalError: (error: boolean) => void;
}

const TeamMember = ({ email, members, setMembers, setMemberRemovalError }: TeamMemberProps) => {
  const { default: trash } = require("../svg/trash.svg") as { default: string };

  function handleRemove() {
    setMemberRemovalError(false);
    const headers = new AxiosHeaders();
    headers["Content-Type"] = "text/plain";
    AuthService.delete(`teams/${email}`, headers)
      .then(() => setMembers(members.filter((m) => m.email !== email)))
      .catch(() => setMemberRemovalError(true));
  }

  return (
    <div className="TeamMember">
      <div className="TeamMember-email">{email}</div>
      <div className="TeamMember-remove">
        <button className="TeamMember-remove-button" onClick={handleRemove} aria-label="Remove member">
          <img src={trash} className="TeamMember-remove-button-image" alt="remove member" />
        </button>
      </div>
    </div>
  );
};

type IntegrationStatus = "loading" | "completed" | "notCompleted";
type MembersStatus = "loading" | "loaded";
type QuestionFrequency = "DAILY" | "WEEKLY" | "MONTHLY";

export const TeamScreen = () => {
  const [members, setMembers] = useState<TeamMemberData[]>([]);
  const [membersStatus, setMembersStatus] = useState<MembersStatus>("loading");
  const [teamRetrieveError, setTeamRetrieveError] = useState(false);

  const [newMemberEmail, setNewMemberEmail] = useState("");
  const [emailFormatError, setEmailFormatError] = useState(false);
  const [existingMemberError, setExistingMemberError] = useState(false);
  const [memberCreationError, setMemberCreationError] = useState(false);
  const [memberRemovalError, setMemberRemovalError] = useState(false);

  const [questionTime, setQuestionTime] = useState("");

  const [integrationStatus, setIntegrationStatus] = useState<IntegrationStatus>("loading");

  const [questionFrequency, setQuestionFrequency] = useState<QuestionFrequency>("DAILY");
  const [questionDayOfWeek, setQuestionDayOfWeek] = useState<string>("MON");
  const [questionDayOfMonth, setQuestionDayOfMonth] = useState<number>(1);
  const [savingSchedule, setSavingSchedule] = useState<boolean>(false);
  const [scheduleOk, setScheduleOk] = useState<string>("");
  const [scheduleErr, setScheduleErr] = useState<string>("");

  /* ----------- Handlers ----------- */
  function handleNewMember() {
    if (!validator.isEmail(newMemberEmail)) {
      setEmailFormatError(true);
      return;
    }

    if (members.some((m) => m.email === newMemberEmail)) {
      setExistingMemberError(true);
      return;
    }

    setEmailFormatError(false);
    setExistingMemberError(false);
    setMemberCreationError(false);

    const headers = new AxiosHeaders();
    headers["Content-Type"] = "text/plain";

    AuthService.post("teams", newMemberEmail, headers)
      .then((response: AxiosResponse<{ members: TeamMemberData[] }>) => {
        setMembers(response.data.members);
        setNewMemberEmail("");
      })
      .catch(() => setMemberCreationError(true));
  }

  function handleNewMemberEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
    setNewMemberEmail(e.target.value);
  }

  function handleQuestionTimeChange(e: React.FormEvent<HTMLInputElement>) {
    const time = (e.target as HTMLInputElement).value;
    setQuestionTime(time);
  }

  async function handleSaveSchedule(e: React.FormEvent) {
    e.preventDefault();
    setScheduleOk("");
    setScheduleErr("");

    if (questionFrequency === "WEEKLY" && !questionDayOfWeek) {
      setScheduleErr("Please select a day of week.");
      return;
    }
    if (questionFrequency === "MONTHLY" && (questionDayOfMonth < 1 || questionDayOfMonth > 28)) {
      setScheduleErr("Day of month must be between 1 and 28.");
      return;
    }

    setSavingSchedule(true);
    try {
      const headers = new AxiosHeaders();
      headers["Content-Type"] = "application/json";

      const payload: any = {
        questionSendingTime: questionTime,       // HH:mm
        questionFrequency                       // DAILY|WEEKLY|MONTHLY
      };
      if (questionFrequency === "WEEKLY") payload.questionDayOfWeek = questionDayOfWeek;
      if (questionFrequency === "MONTHLY") payload.questionDayOfMonth = questionDayOfMonth;

      await AuthService.put("teams/time", JSON.stringify(payload), headers);
      setScheduleOk("Schedule saved.");
    } catch (err: any) {
      setScheduleErr(typeof err?.message === "string" ? err.message : "Unexpected error saving schedule.");
    } finally {
      setSavingSchedule(false);
    }
  }

  /* ----------- Data fetching ----------- */
  useEffect(() => {
    const headers = new AxiosHeaders();

    AuthService.get("teams", headers)
      .then((r) => {
        setMembers(r.data.members);
        setQuestionTime(r.data.questionSendingTime);

        setQuestionFrequency((r.data.questionFrequency as QuestionFrequency) ?? "DAILY");
        setQuestionDayOfWeek(r.data.questionDayOfWeek ?? "MON");
        setQuestionDayOfMonth(Number.isInteger(r.data.questionDayOfMonth) ? r.data.questionDayOfMonth : 1);
      })
      .catch(() => setTeamRetrieveError(true))
      .finally(() => setMembersStatus("loaded"));

    AuthService.get("user/integration", headers)
      .then((r: AxiosResponse<boolean>) => setIntegrationStatus(r.data ? "completed" : "notCompleted"))
      .catch(() => setIntegrationStatus("notCompleted"));
  }, []);

  /* ----------- Render helpers ----------- */
  const renderMembers = () =>
    members.map((m) => (
      <TeamMember
        key={m.id}
        email={m.email}
        members={members}
        setMembers={setMembers}
        setMemberRemovalError={setMemberRemovalError}
      />
    ));

  /* ----------- JSX ----------- */
  return (
    <div className="TeamScreen page">
      {/* --- Members --- */}
      <div className="TeamScreen-members individual">
        <h2>Team members</h2>

        {/* Errors */}
        {teamRetrieveError && <div className="TeamScreen-members-error">Team retrieve error.</div>}
        {existingMemberError && (
          <div className="TeamScreen-members-error">The email is already registered as a team member.</div>
        )}
        {memberCreationError && (
          <div className="TeamScreen-members-error">An unexpected error occurred on member creation.</div>
        )}
        {memberRemovalError && (
          <div className="TeamScreen-members-error">An unexpected error occurred on member removal.</div>
        )}
        {emailFormatError && (
          <div className="TeamScreen-members-newMember-form-errorMessage">Incorrect email format.</div>
        )}

        {/* No members */}
        {membersStatus === "loaded" && !teamRetrieveError && members.length === 0 && (
          <div className="TeamScreen-members-noMembersMessage">Your team has no members yet.</div>
        )}

        {/* Members list */}
        {members.length > 0 && <div className="TeamScreen-members-list">{renderMembers()}</div>}

        {/* Add member */}
        <div className="TeamScreen-members-newMember-form">
          <input
            className="TeamScreen-members-newMember-form-input"
            type="text"
            placeholder="Email"
            autoComplete="email"
            value={newMemberEmail}
            onChange={handleNewMemberEmailChange}
            aria-label="New member email"
          />
          <button className="TeamScreen-members-newMember-form-button" onClick={handleNewMember}>
            Add team member
          </button>
        </div>
      </div>

      {/* --- Slack integration --- */
      <div
        className="individual"
        style={integrationStatus === "loading" ? { minHeight: "130px" } : {}}
      >
        <div className="individual-left">
          <h2>Slack integration</h2>

          {/* Invisible placeholder to preserve height while loading */}
          {integrationStatus === "loading" && (
            <p style={{ visibility: "hidden" }}>
              Placeholder for Slack integration message
            </p>
          )}

          {integrationStatus === "notCompleted" && (
            <p>
              <b>Slack integration has not been completed.</b> Slack is necessary to send the human‑factor questions to
              team members. Click the button to add the Slack App to your workspace. This button will redirect you to
              another page that generates a secured “Add to Slack” button.
            </p>
          )}

          {integrationStatus === "completed" && <p>Slack integration has been completed.</p>}
        </div>

        {/* Right column (button only in "notCompleted", but area always reserved) */}
        <>
          <div className="individual-middle"></div>
          <div className="individual-right">
            {integrationStatus === "notCompleted" && (
              <a className="centered" href={`${process.env.REACT_APP_API_URL}slack/install`}>
                <img
                  alt="Add to Slack"
                  height="40"
                  width="139"
                  src="https://platform.slack-edge.com/img/add_to_slack.png"
                  srcSet="https://platform.slack-edge.com/img/add_to_slack.png 1x, https://platform.slack-edge.com/img/add_to_slack@2x.png 2x"
                />
              </a>
            )}
          </div>
        </>
      </div>

      /* --- Question sending time --- */}
      <div className="individual">
        <div className="individual-left">
          <h2>Question schedule</h2>
          <p>
            Set <b>when</b> and <b>how often</b> the human-factor questions are sent.
            You can choose a time and a schedule (daily, weekly, or monthly).
          </p>
        </div>

        <div className="individual-middle"></div>

        <div className="individual-right individual-right2">

          <div className="ts-panel">
            <form className="TeamScreen-schedule" onSubmit={handleSaveSchedule}>
              <div className="TeamScreen-schedule-grid">

                <label htmlFor="questionFrequency" className="TeamScreen-schedule-label">Frequency</label>
                <div className="select-wrapper">
                  <select
                    id="questionFrequency"
                    value={questionFrequency}
                    onChange={(e) => setQuestionFrequency(e.target.value as any)}
                    className="ts-select"
                  >
                    <option value="DAILY">Daily</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="MONTHLY">Monthly</option>
                  </select>
                </div>

                {questionFrequency === "DAILY" && (
                  <>
                    <span className="TeamScreen-schedule-label"></span>
                    <label className="ts-checkbox">
                      <span>Weekdays only (Mon–Fri)</span>
                    </label>
                  </>
                )}

                {questionFrequency === "WEEKLY" && (
                  <>
                    <label htmlFor="questionDayOfWeek" className="TeamScreen-schedule-label">Day of week</label>
                    <div className="select-wrapper">
                      <select
                        id="questionDayOfWeek"
                        value={questionDayOfWeek}
                        onChange={(e) => setQuestionDayOfWeek(e.target.value)}
                        className="ts-select"
                      >
                        <option value="MONDAY">Monday</option>
                        <option value="TUESDAY">Tuesday</option>
                        <option value="WEDNESDAY">Wednesday</option>
                        <option value="THURSDAY">Thursday</option>
                        <option value="FRIDAY">Friday</option>
                      </select>
                    </div>
                  </>
                )}

                {questionFrequency === "MONTHLY" && (
                  <>
                    <label htmlFor="questionDayOfMonth" className="TeamScreen-schedule-label">Day of month</label>
                    <div className="select-wrapper">
                      <select
                        id="questionDayOfMonth"
                        value={questionDayOfMonth}
                        onChange={(e) => setQuestionDayOfMonth(parseInt(e.target.value, 10))}
                        className="ts-select"
                      >
                        {[...Array(28)].map((_, i) => (
                          <option key={i + 1} value={i + 1}>
                            {i + 1}
                          </option>
                        ))}
                      </select>
                    </div>
                  </>
                )}

                <label htmlFor="questionTime" className="TeamScreen-schedule-label">Time</label>
                <input
                  type="time"
                  id="questionTime"
                  name="questionTime"
                  value={questionTime}
                  onInput={handleQuestionTimeChange}
                  className="ts-input"
                />

              </div>

              <div className="TeamScreen-actions">
                <button type="submit" className="TeamScreen-members-newMember-form-button ts-button" disabled={savingSchedule}>
                  {savingSchedule ? "Saving…" : "Save schedule"}
                </button>
              </div>
              <div className="TeamScreen-actions">
                  {scheduleOk && <span className="ts-success">{scheduleOk}</span>}
                  {scheduleErr && <span className="ts-error">{scheduleErr}</span>}
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
