import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import TextField from "../../components/common/TextField";
import { clearSession, getInitials, getSession, saveSession } from "../../services/session";
import { updateStudent } from "../../services/studentService";
import { friendlyError } from "../../utilis/errorMessage";
import { useToast } from "../../context/useToast";

type FieldName = "firstName" | "lastName" | "email" | "phoneNumber";

function Profile() {
  const navigate = useNavigate();
  const toast = useToast();
  const [session] = useState(getSession);

  const [values, setValues] = useState<Record<FieldName, string>>({
    firstName: session?.firstName ?? "",
    lastName: session?.lastName ?? "",
    email: session?.email ?? "",
    phoneNumber: session?.phoneNumber ?? "",
  });
  const [submitted, setSubmitted] = useState(false);
  const [saving, setSaving] = useState(false);

  const errors: Partial<Record<FieldName, string>> = {};
  if (!values.firstName.trim()) errors.firstName = "Enter your first name.";
  if (!values.lastName.trim()) errors.lastName = "Enter your last name.";
  if (!values.email.trim()) errors.email = "Enter your email address.";
  if (!values.phoneNumber.trim()) errors.phoneNumber = "Enter your phone number.";

  const fieldProps = (name: FieldName) => ({
    id: name,
    value: values[name],
    onChange: (value: string) => setValues((prev) => ({ ...prev, [name]: value })),
    error: submitted ? errors[name] : undefined,
  });

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    if (Object.keys(errors).length > 0 || !session) return;

    try {
      setSaving(true);
      const updated = await updateStudent({
        studentId: session.studentId,
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        email: values.email.trim(),
        phoneNumber: values.phoneNumber.trim(),
      });
      saveSession(updated);
      toast.success("Changes saved.");
    } catch (error) {
      toast.error(friendlyError(error, "We couldn't save your changes. Try again."));
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    clearSession();
    navigate("/login");
  };

  return (
    <div className="page narrow">
      <header className="page-head">
        <div className="row" style={{ justifyContent: "flex-start", gap: 16 }}>
          <span className="avatar" style={{ width: 60, height: 60, fontSize: "1.3rem" }} aria-hidden="true">
            {getInitials()}
          </span>
          <div>
            <h1>Your profile</h1>
            <p>Keep your contact details current so booking reminders reach you.</p>
          </div>
        </div>
      </header>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <div className="form-grid">
          <TextField label="First name" autoComplete="given-name" {...fieldProps("firstName")} />
          <TextField label="Last name" autoComplete="family-name" {...fieldProps("lastName")} />
          <TextField className="span-2" label="Email" type="email" autoComplete="email" {...fieldProps("email")} />
          <TextField className="span-2" label="Phone number" type="tel" autoComplete="tel" {...fieldProps("phoneNumber")} />
        </div>
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? "Saving..." : "Save changes"}</button>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>Log out</button>
        </div>
      </form>
    </div>
  );
}

export default Profile;
