import { useState } from "react";
import type { FormEvent} from "react";

function Profile() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!firstName || !lastName || !email || !phoneNumber) {
      setMessage("Please complete all required fields.");
      return;
    }

    setMessage("Profile updated successfully.");
  };

  return (
    <div>
      <h1>Student Profile</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="firstName">First Name</label>
          <br />
          <input
            id="firstName"
            type="text"
            value={firstName}
            onChange={(event) => setFirstName(event.target.value)}
            placeholder="Enter your first name"
          />
        </div>

        <br />

        <div>
          <label htmlFor="lastName">Last Name</label>
          <br />
          <input
            id="lastName"
            type="text"
            value={lastName}
            onChange={(event) => setLastName(event.target.value)}
            placeholder="Enter your last name"
          />
        </div>

        <br />

        <div>
          <label htmlFor="email">Email</label>
          <br />
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Enter your email"
          />
        </div>

        <br />

        <div>
          <label htmlFor="phoneNumber">Phone Number</label>
          <br />
          <input
            id="phoneNumber"
            type="tel"
            value={phoneNumber}
            onChange={(event) => setPhoneNumber(event.target.value)}
            placeholder="Enter your phone number"
          />
        </div>

        <br />

        {message && <p>{message}</p>}

        <button type="submit">Save Changes</button>
      </form>
    </div>
  );
}

export default Profile;