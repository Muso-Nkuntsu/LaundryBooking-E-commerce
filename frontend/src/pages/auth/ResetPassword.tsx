import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { resetPassword } from "../../services/studentService";
import { friendlyError } from "../../utilis/errorMessage";

// The same rule as the backend (PasswordHasher.requireStrong); the backend still checks it.
const isStrong = (password: string) => password.length >= 8 && /[A-Za-z]/.test(password) && /\d/.test(password);

// The page the link in the reset email opens: /reset-password?token=...
function ResetPassword() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  // Read the token once, then take it out of the address bar so it isn't left in the browser history.
  const [token] = useState(() => params.get("token") ?? "");

  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [errors, setErrors] = useState<{ password?: string; confirm?: string }>({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (params.get("token")) window.history.replaceState(null, "", "/reset-password");
  }, [params]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setServerError("");

    const next: { password?: string; confirm?: string } = {};
    if (!isStrong(password)) next.password = "Use at least 8 characters, with a letter and a number.";
    if (password !== confirm) next.confirm = "The passwords don't match.";
    setErrors(next);
    if (Object.keys(next).length > 0) return;

    try {
      setSaving(true);
      await resetPassword(token, password);
      navigate("/login", { replace: true, state: { passwordReset: true } });
    } catch (error) {
      setServerError(friendlyError(error, "We couldn't change your password. Try again."));
    } finally {
      setSaving(false);
    }
  };

  if (!token) {
    return (
      <AuthLayout>
        <h2>Reset your password</h2>
        <div className="alert alert-error" role="alert">
          This reset link is incomplete. Ask for a new one.
        </div>
        <p className="auth-foot">
          <Link to="/forgot-password">Send a new link</Link>
        </p>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <h2>Choose a new password</h2>

      <form className="stack" onSubmit={handleSubmit} noValidate>
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError} <Link to="/forgot-password">Send a new link</Link>
          </div>
        )}
        <TextField
          id="password"
          label="New password"
          type="password"
          autoComplete="new-password"
          hint="At least 8 characters, with a letter and a number."
          value={password}
          onChange={setPassword}
          error={errors.password}
        />
        <TextField
          id="confirm"
          label="Confirm new password"
          type="password"
          autoComplete="new-password"
          value={confirm}
          onChange={setConfirm}
          error={errors.confirm}
        />
        <button type="submit" className="btn btn-primary btn-block" disabled={saving}>
          {saving ? "Saving..." : "Change password"}
        </button>
      </form>

      <p className="auth-foot">
        <Link to="/login">Back to log in</Link>
      </p>
    </AuthLayout>
  );
}

export default ResetPassword;
