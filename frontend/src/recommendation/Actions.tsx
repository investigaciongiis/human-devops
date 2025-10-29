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