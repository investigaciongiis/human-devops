import { AxiosHeaders } from "axios";
import { ReactElement, useEffect, useState } from "react";
import AuthService from "../authentication/AuthService";
import "./Actions.css";

interface ActionProps {
  title: string;
  description: string;
  score: number;
}

interface ActionListProps {
  actions: ActionProps[];
}

function Action(props: Readonly<ActionProps>) {
  return (
    <div className="Action">
      <div className="Action-header">
        <span className="Action-title">{props.title}</span>

        <span
          className="Action-score"
          aria-label={`Score ${(props.score * 100).toFixed(0)} percent`}
        >
          {(props.score * 100).toFixed(0)}%
        </span>
      </div>

      <p className="Action-description">{props.description}</p>
    </div>
  );
}

function ActionList(props: ActionListProps) : ReactElement[] {
  const rows: ReactElement[] = [];
  console.log(props);
  if (props === undefined || props === null || props.actions.length === 0) {
    console.error("ActionList: the action list parameter is empty, undefined or null");
    return rows;
  }
  props.actions.sort((a, b) => {
    return b.score - a.score;
  });
  props.actions.forEach((action: ActionProps) => {
    rows.push(
      <Action
        title={action.title}
        score={action.score}
        description={action.description}
      />
    );
  });
  return rows;
}

export default function ActionScreen(): ReactElement | null {
  const [actions, setActions] = useState<ActionProps[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [retrieveError, setRetrieveError] = useState(false);

  async function downloadActions(format: "csv" | "json" | "xlsx") {
    try {
      const headers = new AxiosHeaders();

      if (format === "xlsx") {
        const url = `${process.env.REACT_APP_API_URL}/actions/export?format=${format}`;
        const token = AuthService.getToken();
        headers["Accept"] = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        const res = await fetch(url, {
          headers: {
            Accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        });

        if (!res.ok) {
          throw new Error(`Error downloading XLSX: ${res.status} ${res.statusText}`);
        }

        const blob = await res.blob();

        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        const ts = new Date();
        const pad = (n:number)=> String(n).padStart(2,"0");
        const filename = `human-factors_${ts.getFullYear()}-${pad(ts.getMonth()+1)}-${pad(ts.getDate())}T${pad(ts.getHours())}-${pad(ts.getMinutes())}-${pad(ts.getSeconds())}.xlsx`;
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
        return;
      }

      const url = `actions/export?format=${format}`;

      headers["Accept"] = format === "csv" ? "text/csv" : "application/json";

      const response = await AuthService.get(url, headers);

      const jsonString =
        typeof response.data === "string" ? response.data : JSON.stringify(response.data, null, 2);

      const blob = new Blob(
        [jsonString],
        { type: format === "csv" ? "text/csv;charset=utf-8" : "application/json;charset=utf-8" }
      );

      const date = new Date();
      const pad = (n: number) => n.toString().padStart(2, "0");

      const year = date.getFullYear();
      const month = pad(date.getMonth() + 1);
      const day = pad(date.getDate());
      const hours = pad(date.getHours());
      const minutes = pad(date.getMinutes());
      const seconds = pad(date.getSeconds());

      const formattedDate = `${year}-${month}-${day}_${hours}-${minutes}-${seconds}`;
    
      const fileName = `recommendations_${formattedDate}.${format}`;

      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = fileName;
      link.click();
      URL.revokeObjectURL(link.href);
    } catch (e) {
      console.error("Error downloading file:", e);
      alert("Error downloading file");
    }
  }

  useEffect(() => {
    AuthService.get("teams/actions", new AxiosHeaders())
      .then((res) => setActions(res.data ?? []))
      .catch((err) => {
        console.error(err);
        setRetrieveError(true);
      })
      .finally(() => setLoaded(true));
  }, []);

  if (!loaded){
    return (
      <div className="container-full">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  const anyActions = actions.length > 0;

  return (

    <div className="Actions page">
      <div className="graph-toolbar2">
            <button className="graph-btn2" onClick={() => downloadActions("xlsx")}>
              Export XLSX
            </button>
            <button className="graph-btn2" onClick={() => downloadActions("csv")}>
              Export CSV
            </button>
            <button className="graph-btn2" onClick={() => downloadActions("json")}>
              Export JSON
            </button>
          </div>
      {retrieveError && (
        <div className="error">
          <h3>Retrieve error</h3>
        </div>
      )}

      {!retrieveError && !anyActions && (
        <div className="error">
          <h3>Incomplete team measurement</h3>
          <p>
            Your team does not have enough human factor measurements to provide
            any action recommendations. Keep answering daily questions and
            recommendations will start to appear.
          </p>
        </div>
      )}

      {!retrieveError && anyActions && (
        <div className="no-error">
          <h3 className="Actions-title">Actions ordered by recommendation priority</h3>
          <div className="Actions-list">
            <ActionList actions={actions} />
          </div>
        </div>
      )}
    </div>
  );
}