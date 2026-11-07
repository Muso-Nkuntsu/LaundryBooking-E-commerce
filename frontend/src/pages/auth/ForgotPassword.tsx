import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { requestPasswordReset } from "../../services/studentService";
import { friendlyError } from "../../utilis/errorMessage";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Asks for a password reset link. The reply is the same whether or not the email has an account.
function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [serverError, setServerError] = useState("");
  const [notice, setNotice] = useState("");
  const [sending, setSending] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setServerError("");

    if (!EMAIL_PATTERN.test(email.trim())) {
      setEmailError("Enter the email address you registered with.");
      return;
    }
    setEmailError("");

    try {
      setSending(true);
      const response = await requestPasswordReset(email.trim());
      setNotice(response.message);
    } catch (error) {
      setServerError(friendlyError(error, "We couldn't send the email. Try again."));
    } finally {
      setSending(false);
    }
  };

  return (
    <AuthLayout>
      <h2>Reset your password</h2>
      <p className="muted">We'll email you a link to choose a new password.</p>

      {notice ? (
        <div className="stack">
          <div className="alert alert-ok" role="status">
            {notice}
          </div>
          <p className="muted small">The link works for 30 minutes and can be used once. Check your spam folder too.</p>
        </div>
      ) : (
        <form className="stack" onSubmit={handleSubmit} noValidate>
          {serverError && (
            <div className="alert alert-error" role="alert">
              {serverError}
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
          <button type="submit" className="btn btn-primary btn-block" disabled={sending}>
            {sending ? "Sending..." : "Send reset link"}
          </button>
        </form>
      )}

      <p className="auth-foot">
        <Link to="/login">Back to log in</Link>
      </p>
    </AuthLayout>
  );
}

export default ForgotPassword;
