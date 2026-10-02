import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import TextField from "../../components/common/TextField";
import { clearSession, getInitials, getSession, saveSession } from "../../services/session";
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

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    if (Object.keys(errors).length > 0) return;

    // Saved on this device only until the backend has an update-student endpoint.
    saveSession({ ...session, ...values });
    toast.success("Changes saved.");
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
          <button type="submit" className="btn btn-primary">Save changes</button>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>Log out</button>
        </div>
      </form>
    </div>
  );
}

export default Profile;
