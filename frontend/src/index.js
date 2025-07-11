import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createBrowserRouter, redirect } from "react-router-dom";
import AuthService from "./authentication/AuthService";
import ErrorPage from "./error-page";
import GraphScreen from "./graph/GraphScreen";
import "./index.css";
import Root from "./interface/Root";
import LoginScreen from "./login/LoginScreen";
import SignupScreen from "./login/SignupScreen";
import ActionScreen from "./recommendation/Actions";
import reportWebVitals from "./reportWebVitals";
import SettingsScreen from "./settings/SettingsScreen";
import { TeamScreen } from "./team/TeamScreen";

const router = createBrowserRouter(
  [
    {
      path: "/",
      element: <Root />,
      errorElement: <ErrorPage />,
      loader: () => {
        if(!AuthService.getCurrentUser() || AuthService.getCurrentUser() === undefined || AuthService.getCurrentUser() === "undefined") {
          console.log("redirection problem");
          return redirect("/login");
        }
        return null;
      },
      children: [
        {
          path: "graph",
          element: <GraphScreen/>
        },
        {
          path: "team",
          element: <TeamScreen/>
        },
        {
          path: "actions",
          element: <ActionScreen/>
        },
        {
          path: "settings",
          element: <SettingsScreen />
        },
      ]
    },
    {
      path: "login",
      element: <LoginScreen />,
    },
    {
      path: "signup",
      element: <SignupScreen />,
    },
    {},
  ]);

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<RouterProvider router={router} />);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
