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
      <div className="Action-title">
        {props.title} {props.score}
      </div>
      <div className="Action-description">{props.description}</div>
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

export default function ActionScreen() : ReactElement {
  const [actions, setActions] = useState<ActionProps[]>([]);
  const [actionsRetrieveError, setActionsRetrieveError] =
    useState<boolean>(false);
  const [anyActions, setAnyActions] = useState<boolean>(false);

  useEffect(() => {
    AuthService
      .get("teams/actions", new AxiosHeaders())
      .then((response) => {
        setActions(response.data);
        if (
          response.data === undefined ||
          response.data.length === undefined ||
          response.data.length === 0
        ) {
          setAnyActions(false);
        } else {
          setAnyActions(true);
        }
      })
      .catch((error) => {
        setActionsRetrieveError(true);
        console.error(error);
      });
  }, []);

  return (
    <div className="Actions page">
      <div className="error" hidden={!actionsRetrieveError}>
        <h3 className="error">Retrieve error</h3>
      </div>
      <div className="error" hidden={anyActions}>
        <h3 className="error">Incomplete team measurement</h3>
        <p>
          Your team does not have enough human factor measurements to provide
          any action recommendations. Keep answering daily questions and
          recommendations will start to appear.
        </p>
      </div>
      <div className="no-error" hidden={actionsRetrieveError || !anyActions}>
        <h3 className="Actions-title">Recommended actions</h3>
        <div className="Actions-list">
          <ActionList actions={actions} />
        </div>
      </div>
    </div>
  );
}
