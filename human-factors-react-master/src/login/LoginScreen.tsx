import { useState } from "react";
import { useNavigate } from "react-router-dom";
import validator from "validator";
import AuthService from "../authentication/AuthService";
import "./LoginScreen.css";

export default function LoginScreen() {
  const { default: users } = require("../svg/users.svg") as { default: string };

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState("typing");
  const [emailFormatError, setEmailFormatError] = useState(false);
  const [loginError, setLoginError] = useState(false);
  const [unknownError, setUnknownError] = useState(false);

  function handleEmailChange(e: any) {
    setEmail(e.target.value);
  }

  function handlePasswordChange(e: any) {
    setPassword(e.target.value);
  }

  function handleSubmit(e: any) {
    e.preventDefault();
    if (!validator.isEmail(email)) {
      setEmailFormatError(true);
      return;
    }
    setEmailFormatError(false);
    setLoginError(false);
    setUnknownError(false);
    setStatus("submitting");
    AuthService.login(email, password)
      .then((response) => {
        navigate("/team");
      })
      .catch((error) => {
        if (error.response && error.response.status === 404) {
          setLoginError(true);
        } else {
          setUnknownError(true);
        }
        setStatus("typing");
      });
  }

  return (
    <div className="LoginScreen">
      <div className="LoginScreen-menu">
        <img src={users} className="LoginScreen-menu-icon" alt="" />
        <h2 className="LoginScreen-menu-name">Human DevOps</h2>
        <form className="LoginScreen-menu-login-form" onSubmit={handleSubmit}>
          <div className="LoginScreen-menu-login-form-error">
            <div
              className="LoginScreen-menu-login-form-error-message"
              hidden={!emailFormatError}
            >
              Incorrect email format.
            </div>
            <div
              className="LoginScreen-menu-login-form-error-message"
              hidden={!loginError}
            >
              Incorrect email or password.
            </div>
            <div
              className="LoginScreen-menu-login-form-error-message"
              hidden={!unknownError}
            >
              Unexpected error when connecting with the application.
            </div>
          </div>
          <input
            className="LoginScreen-menu-login-form-input"
            type="text"
            id="email"
            name="email"
            placeholder="Email"
            value={email}
            autoComplete="username"
            onChange={handleEmailChange}
          />
          <input
            className="LoginScreen-menu-login-form-input"
            type="password"
            id="password"
            name="password"
            placeholder="Password"
            autoComplete="current-password"
            value={password}
            onChange={handlePasswordChange}
          />
          <button
            type="submit"
            className="LoginScreen-menu-login-form-button"
            disabled={
              email.length === 0 ||
              password.length === 0 ||
              status === "submitting"
            }
          >
            Login
          </button>
        </form>
        <div className="LoginScreen-menu-signup">
          <p>Not a member?</p>
          <a className="LoginScreen-menu-signup-button link" href="/signup">
            Sign up
          </a>
        </div>
      </div>
    </div>
  );
}
