import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../../components/layout/AuthLayout";
import TextField from "../../components/common/TextField";
import { createStudent } from "../../services/studentService";
import { saveSession } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { useToast } from "../../context/useToast";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_PATTERN = /^\+?[0-9\s-]{9,15}$/;
const STRENGTH_LABEL = ["", "Weak", "Fair", "Good", "Strong"];

type FieldName = "firstName" | "lastName" | "email" | "phoneNumber" | "password" | "confirmPassword";
type Values = Record<FieldName, string>;
type Errors = Partial<Record<FieldName, string>>;

const EMPTY: Values = { firstName: "", lastName: "", email: "", phoneNumber: "", password: "", confirmPassword: "" };

function passwordStrength(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 6) score += 1;
  if (password.length >= 10) score += 1;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score += 1;
  if (/\d/.test(password) && /[^A-Za-z0-9]/.test(password)) score += 1;
  return Math.max(score, 1);
}

function validate(values: Values): Errors {
  const errors: Errors = {};
  if (!values.firstName.trim()) errors.firstName = "Enter your first name.";
  if (!values.lastName.trim()) errors.lastName = "Enter your last name.";
  if (!values.email.trim()) errors.email = "Enter your email address.";
  else if (!EMAIL_PATTERN.test(values.email.trim())) errors.email = "That doesn't look like an email address.";
  if (!values.phoneNumber.trim()) errors.phoneNumber = "Enter your phone number.";
  else if (!PHONE_PATTERN.test(values.phoneNumber.trim())) errors.phoneNumber = "Use digits only, for example 082 123 4567.";
  if (!values.password) errors.password = "Choose a password.";
  else if (values.password.length < 6) errors.password = "Use at least 6 characters.";
  if (!values.confirmPassword) errors.confirmPassword = "Type your password again.";
  else if (values.password !== values.confirmPassword) errors.confirmPassword = "The passwords don't match.";
  return errors;
}

function Register() {
  const navigate = useNavigate();
  const toast = useToast();

  const [values, setValues] = useState<Values>(EMPTY);
  const [touched, setTouched] = useState<Partial<Record<FieldName, boolean>>>({});
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");

  const errors = validate(values);
  const strength = passwordStrength(values.password);

  const fieldProps = (name: FieldName) => ({
    id: name,
    value: values[name],
    onChange: (value: string) => setValues((prev) => ({ ...prev, [name]: value })),
    onBlur: () => setTouched((prev) => ({ ...prev, [name]: true })),
    error: submitted || touched[name] ? errors[name] : undefined,
  });

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    setServerError("");

    if (Object.keys(errors).length > 0) return;

    const student = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      email: values.email.trim(),
      phoneNumber: values.phoneNumber.trim(),
    };

    try {
      setSubmitting(true);
      const created = await createStudent({ ...student, password: values.password });
      const studentId = typeof created?.studentId === "number" ? created.studentId : undefined;
      saveSession({ ...student, studentId });
      toast.success("Account created. Log in to continue.");
      navigate("/login");
    } catch (error) {
      setServerError(friendlyError(error, "We couldn't create your account. Try again."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <h2>Create your account</h2>
      <p className="muted">It takes a minute, then you can book a machine.</p>

      <form className="stack" onSubmit={handleSubmit} noValidate>
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError}
          </div>
        )}

        <div className="form-grid">
          <TextField label="First name" autoComplete="given-name" {...fieldProps("firstName")} />
          <TextField label="Last name" autoComplete="family-name" {...fieldProps("lastName")} />
          <TextField className="span-2" label="Email" type="email" autoComplete="email" placeholder="you@university.ac.za" {...fieldProps("email")} />
          <TextField className="span-2" label="Phone number" type="tel" autoComplete="tel" placeholder="082 123 4567" {...fieldProps("phoneNumber")} />
          <div className="span-2">
            <TextField label="Password" type="password" autoComplete="new-password" hint="At least 6 characters." {...fieldProps("password")} />
            {values.password && (
              <div className="row small muted" style={{ marginTop: 8 }}>
                <div className="strength" data-level={strength} style={{ flex: 1 }} aria-hidden="true">
                  <span /><span /><span /><span />
                </div>
                <span aria-live="polite">{STRENGTH_LABEL[strength]}</span>
              </div>
            )}
          </div>
          <TextField className="span-2" label="Confirm password" type="password" autoComplete="new-password" {...fieldProps("confirmPassword")} />
        </div>

        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? "Creating account..." : "Create account"}
        </button>
      </form>

      <p className="auth-foot">
        Already registered? <Link to="/login">Log in</Link>
      </p>
    </AuthLayout>
  );
}

export default Register;
