import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { ApiError } from "../../services/Api";
import { saveSession } from "../../services/session";
import { loginStudent, resendVerificationEmail } from "../../services/studentService";
import { friendlyError } from "../../utilis/errorMessage";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const routeState = location.state as { email?: string; justRegistered?: boolean } | null;
  const registeredEmail = routeState?.email;

  const [email, setEmail] = useState(registeredEmail ?? "");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [serverError, setServerError] = useState("");
  const [notice, setNotice] = useState(
    routeState?.justRegistered
      ? "Account created. We've sent a verification link to your email. Open it, then log in."
      : "",
  );
  const [needsVerification, setNeedsVerification] = useState(false);
  const [resending, setResending] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleResend = async () => {
    if (!EMAIL_PATTERN.test(email.trim())) {
      setErrors({ email: "Enter the email address you registered with." });
      return;
    }
    try {
      setResending(true);
      setServerError("");
      const response = await resendVerificationEmail(email.trim());
      setNotice(response.message);
      setNeedsVerification(false);
    } catch (error) {
      setServerError(friendlyError(error, "We couldn't send the email. Try again."));
    } finally {
      setResending(false);
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setServerError("");
    setNotice("");
    setNeedsVerification(false);

    const next: { email?: string; password?: string } = {};
    if (!email.trim()) next.email = "Enter your email address.";
    else if (!EMAIL_PATTERN.test(email.trim())) next.email = "That doesn't look like an email address.";
    if (!password) next.password = "Enter your password.";

    setErrors(next);
    if (Object.keys(next).length > 0) return;

    try {
      setSubmitting(true);
      const student = await loginStudent(email.trim(), password);
      saveSession(student);
      navigate("/dashboard");
    } catch (error) {
      // 403 means the password was right but the email address is not verified yet.
      setNeedsVerification(error instanceof ApiError && error.status === 403);
      setServerError(friendlyError(error, "We couldn't log you in. Try again."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <h2>Log in</h2>
      <p className="muted">Use your student email to see your bookings.</p>

      <form className="stack" onSubmit={handleSubmit} noValidate>
        {notice && (
          <div className="alert alert-ok" role="status">
            {notice}
          </div>
        )}
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError}
            {needsVerification && (
              <>
                {" "}
                <button type="button" className="link-btn small" onClick={handleResend} disabled={resending}>
                  {resending ? "Sending..." : "Send a new verification email"}
                </button>
              </>
            )}
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
          error={errors.email}
        />
        <TextField
          id="password"
          label="Password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={setPassword}
          error={errors.password}
        />
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? "Logging in..." : "Log in"}
        </button>
      </form>

      <p className="auth-foot">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </AuthLayout>
  );
}

export default Login;
