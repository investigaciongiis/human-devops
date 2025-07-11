import { useState } from "react";
import { useNavigate } from "react-router-dom";
import validator from "validator";
import AuthService from "../authentication/AuthService";
import "./SignupScreen.css";

export default function SignupScreen() {

  const {default: users} = require("../svg/users.svg") as {default: string};

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [emailFormatError, setEmailFormatError] = useState(false);
  const [passwordFormatError, setPasswordFormatError] = useState(false);
  const [passwordRepetitionError, setPasswordRepetitionError] = useState(false);
  const [existingUserError, setExistingUserError] = useState(false);
  const [unknownError, setUnknownError] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function handleEmailChange(e : any) {
    setEmail(e.target.value);
  }

  function handlePasswordChange(e : any) {
    setPassword(e.target.value);
  }

  function handlePassword2Change(e : any) {
    setPassword2(e.target.value);
  }

  function validPassword(password : string) {
    return password.length > 5;
  }

  function handleSubmit(e : any) {
    e.preventDefault();
    if (!validator.isEmail(email)) {
      setEmailFormatError(true);
      return;
    }
    setEmailFormatError(false);
    if (!validPassword(password)) {
      setPasswordFormatError(true);
      return;
    }
    setPasswordFormatError(false);
    if (password !== password2) {
      setPasswordRepetitionError(true);
      return;
    }
    setPasswordRepetitionError(false);
    setUnknownError(false);
    setSubmitting(true);
    AuthService.register(email, password)
      .then(() => navigate("/login"))
      .catch((error) => {
        setSubmitting(false);
        if (error.response && error.response.status === 400) {
          setExistingUserError(true);
        } else {
          setUnknownError(true);
        }
      });
  }

  return (
    <div className="SignupScreen">
      <div className="SignupScreen-menu">
        <img src={users} className="SignupScreen-menu-icon" alt="" />
        <h2 className="SignupScreen-menu-title">Register</h2>
        <form
          className="SignupScreen-menu-signup-form"
          onSubmit={handleSubmit}
        >
          <div className="SignupScreen-menu-signup-form-error">
            <div
              className="SignupScreen-menu-signup-form-error-message"
              hidden={!emailFormatError}
            >
              Incorrect email format.
            </div>
            <div
              className="SignupScreen-menu-signup-form-error-message"
              hidden={!passwordFormatError}
            >
              The password has to be at least 6 characters long.
            </div>
            <div
              className="SignupScreen-menu-signup-form-error-message"
              hidden={!passwordRepetitionError}
            >
              Passwords do not match.
            </div>
            <div
              className="SignupScreen-menu-signup-form-error-message"
              hidden={!existingUserError}
            >
              The email is already registered in the application.
            </div>
            <div
              className="SignupScreen-menu-signup-form-error-message"
              hidden={!unknownError}
            >
              Unexpected error when connecting with the application.
            </div>
          </div>
          <input
            className="SignupScreen-menu-signup-form-input"
            type="text"
            id="email"
            name="email"
            placeholder="Email"
            value={email}
            onChange={handleEmailChange}
          />
          <input
            className="SignupScreen-menu-signup-form-input"
            type="password"
            id="password"
            name="password"
            placeholder="Password"
            autoComplete="new-password"
            value={password}
            onChange={handlePasswordChange}
          />
          <input
            className="SignupScreen-menu-signup-form-input"
            type="password"
            id="repeat-password"
            name="repeat-password"
            placeholder="Repeat password"
            autoComplete="new-password"
            value={password2}
            onChange={handlePassword2Change}
          />
          <button
            type="submit"
            className="SignupScreen-menu-signup-form-button"
            disabled={submitting}
          >
            Sign up
          </button>
        </form>
        <div className="SignupScreen-menu-login">
          <p>Have an account?</p>
          <a className="SignupScreen-menu-login-button link" href="/login">
            Log in
          </a>
        </div>
      </div>
    </div>
  );
}
