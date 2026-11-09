import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { resendVerificationEmail, verifyEmail } from "../../services/studentService";
import { friendlyError } from "../../utilis/errorMessage";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type Status = "verifying" | "success" | "error";

// The page the link in the verification email opens: /verify-email?token=...
function VerifyEmail() {
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";

  const [status, setStatus] = useState<Status>(token ? "verifying" : "error");
  const [message, setMessage] = useState(token ? "" : "This verification link is incomplete. Request a new one below.");

  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [resending, setResending] = useState(false);
  const [resendNotice, setResendNotice] = useState("");
  const [resendError, setResendError] = useState("");

  // React runs effects twice in development; only send the token once.
  const started = useRef(false);

  useEffect(() => {
    if (!token || started.current) return;
    started.current = true;

    verifyEmail(token)
      .then((response) => {
        setStatus("success");
        setMessage(response.message);
      })
      .catch((error) => {
        setStatus("error");
        setMessage(friendlyError(error, "We couldn't verify your email. Try again."));
      });
  }, [token]);

  const handleResend = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setResendNotice("");
    setResendError("");

    if (!EMAIL_PATTERN.test(email.trim())) {
      setEmailError("Enter the email address you registered with.");
      return;
    }
    setEmailError("");

    try {
      setResending(true);
      const response = await resendVerificationEmail(email.trim());
      setResendNotice(response.message);
    } catch (error) {
      setResendError(friendlyError(error, "We couldn't send the email. Try again."));
    } finally {
      setResending(false);
    }
  };

  return (
    <AuthLayout>
      <h2>Verify your email</h2>

      {status === "verifying" && <p className="muted" role="status">Verifying your email address...</p>}

      {status === "success" && (
        <div className="stack">
          <div className="alert alert-ok" role="status">
            {message}
          </div>
          <Link to="/login" className="btn btn-primary btn-block">
            Go to log in
          </Link>
        </div>
      )}

      {status === "error" && (
        <form className="stack" onSubmit={handleResend} noValidate>
          <div className="alert alert-error" role="alert">
            {message}
          </div>
          {resendNotice && (
            <div className="alert alert-ok" role="status">
              {resendNotice}
            </div>
          )}
          {resendError && (
            <div className="alert alert-error" role="alert">
              {resendError}
            </div>
          )}
          <TextField
            id="email"
            label="Email"
            type="email"
            autoComplete="email"
            placeholder="you@university.ac.za"
            value={email}
            onChange={setEmail}
            error={emailError}
          />
          <button type="submit" className="btn btn-primary btn-block" disabled={resending}>
            {resending ? "Sending..." : "Send a new verification email"}
          </button>
          <p className="auth-foot">
            <Link to="/login">Back to log in</Link>
          </p>
        </form>
      )}
    </AuthLayout>
  );
}

export default VerifyEmail;
