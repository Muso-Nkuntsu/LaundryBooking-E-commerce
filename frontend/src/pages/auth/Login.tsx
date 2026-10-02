import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { getSession, saveSession } from "../../services/session";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState(() => getSession()?.email ?? "");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const next: { email?: string; password?: string } = {};
    if (!email.trim()) next.email = "Enter your email address.";
    else if (!EMAIL_PATTERN.test(email.trim())) next.email = "That doesn't look like an email address.";
    if (!password) next.password = "Enter your password.";

    setErrors(next);
    if (Object.keys(next).length > 0) return;

    // Temporary login behaviour.
    // We will replace this with the backend API later.
    const existing = getSession();
    if (!existing || existing.email !== email.trim()) {
      saveSession({ email: email.trim() });
    }
    navigate("/dashboard");
  };

  return (
    <AuthLayout>
      <h2>Log in</h2>
      <p className="muted">Use your student email to see your bookings.</p>

      <form className="stack" onSubmit={handleSubmit} noValidate>
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
        <button type="submit" className="btn btn-primary btn-block">
          Log in
        </button>
      </form>

      <p className="auth-foot">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </AuthLayout>
  );
}

export default Login;
