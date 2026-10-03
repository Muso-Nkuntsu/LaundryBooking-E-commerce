import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation } from "react-router-dom";
import TextField from "../../components/common/TextField";
import Icon from "../../components/common/Icon";
import { createBookingPayment } from "../../services/paymentService";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

type Method = "card" | "eft";

function formatCardNumber(raw: string): string {
  return raw.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiry(raw: string): string {
  const digits = raw.replace(/\D/g, "").slice(0, 4);
  return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
}

function Payment() {
  const location = useLocation();
  const state = (location.state ?? {}) as { amount?: number; description?: string; bookingId?: number };
  const amount = typeof state.amount === "number" ? state.amount : 0;

  const [method, setMethod] = useState<Method>("card");
  const [cardNumber, setCardNumber] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [cvv, setCvv] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [paid, setPaid] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");

  const errors: { cardNumber?: string; expiryDate?: string; cvv?: string } = {};
  if (method === "card") {
    if (cardNumber.replace(/\s/g, "").length < 13) errors.cardNumber = "Enter the full card number.";
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) errors.expiryDate = "Use the format MM/YY.";
    if (!/^\d{3,4}$/.test(cvv)) errors.cvv = "Enter the 3 digits on the back of the card.";
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    setServerError("");
    if (Object.keys(errors).length > 0) return;

    // No payment gateway is connected, so nothing is charged and card details are not sent.
    // For a booking with an amount, the payment is recorded on the backend as PENDING.
    if (state.bookingId && amount > 0) {
      try {
        setSubmitting(true);
        await createBookingPayment(state.bookingId, amount, method === "card" ? "CARD" : "EFT");
      } catch (error) {
        setServerError(friendlyError(error, "We couldn't record your payment. Try again."));
        return;
      } finally {
        setSubmitting(false);
      }
    }

    setPaid(true);
  };

  if (paid) {
    return (
      <div className="page narrow">
        <div className="card state">
          <span className="tick"><Icon name="check" size={36} /></span>
          <h1>Payment submitted</h1>
          <p>{formatCurrency(amount)} by {method === "card" ? "card" : "EFT"}. You'll get a notification once it is confirmed.</p>
          <div className="btn-row">
            <Link to="/my-bookings" className="btn btn-primary">View my bookings</Link>
            <Link to="/dashboard" className="btn btn-ghost">Back to home</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Payment</h1>
          <p>Choose how you want to pay.</p>
        </div>
      </header>

      <section className="card row">
        <div>
          <span className="muted small">Amount due</span>
          <div className="price">{formatCurrency(amount)}</div>
        </div>
        {state.description && <span className="muted">{state.description}</span>}
      </section>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
          <legend style={{ fontWeight: 600, marginBottom: 8, padding: 0 }}>Payment method</legend>
          <div className="choice-row">
            {(["card", "eft"] as const).map((option) => (
              <label key={option} className={`choice ${method === option ? "selected" : ""}`}>
                <input type="radio" name="method" value={option} checked={method === option} onChange={() => setMethod(option)} />
                {option === "card" ? "Card" : "EFT"}
              </label>
            ))}
          </div>
        </fieldset>

        {method === "card" ? (
          <div className="form-grid">
            <TextField
              className="span-2"
              id="cardNumber"
              label="Card number"
              inputMode="numeric"
              autoComplete="cc-number"
              placeholder="1234 5678 9012 3456"
              value={cardNumber}
              onChange={(value) => setCardNumber(formatCardNumber(value))}
              error={submitted ? errors.cardNumber : undefined}
            />
            <TextField
              id="expiryDate"
              label="Expiry date"
              inputMode="numeric"
              autoComplete="cc-exp"
              placeholder="MM/YY"
              value={expiryDate}
              onChange={(value) => setExpiryDate(formatExpiry(value))}
              error={submitted ? errors.expiryDate : undefined}
            />
            <TextField
              id="cvv"
              label="CVV"
              type="password"
              inputMode="numeric"
              autoComplete="cc-csc"
              maxLength={4}
              value={cvv}
              onChange={(value) => setCvv(value.replace(/\D/g, ""))}
              error={submitted ? errors.cvv : undefined}
            />
          </div>
        ) : (
          <div className="alert alert-info">
            Pay by EFT from your banking app, then submit here so we know to expect it.
          </div>
        )}

        {serverError && <div className="alert alert-error" role="alert">{serverError}</div>}

        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? "Submitting..." : `Pay ${formatCurrency(amount)}`}
        </button>
      </form>
    </div>
  );
}

export default Payment;
