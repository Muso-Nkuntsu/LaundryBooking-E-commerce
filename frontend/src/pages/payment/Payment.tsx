import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation } from "react-router-dom";
import TextField from "../../components/common/TextField";
import Icon from "../../components/common/Icon";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { EFT_ACCOUNT, EFT_CLEARING_TIME } from "../../config";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { bookingService } from "../../services/bookingService";
import { orderService } from "../../services/orderService";
import { createPayment, eftReference, getPaymentsFor, isAwaitingVerification } from "../../services/paymentService";
import type { PaymentRecord, PaymentTarget } from "../../services/paymentService";
import { getStudentId } from "../../services/session";
import { bookingDate } from "../../utilis/bookingDisplay";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

type Method = "card" | "eft";

interface PaymentLocationState {
  bookingId?: number;
  orderId?: number;
  amount?: number;
  description?: string;
}

const SA_BANKS = ["Absa", "African Bank", "Capitec", "Discovery Bank", "FNB", "Investec", "Nedbank", "Standard Bank", "TymeBank", "Other"];

function targetFromState(state: PaymentLocationState): PaymentTarget | null {
  const amount = typeof state.amount === "number" ? state.amount : 0;
  if (state.bookingId) return { kind: "booking", id: state.bookingId, amount, description: state.description ?? `Booking #${state.bookingId}` };
  if (state.orderId) return { kind: "order", id: state.orderId, amount, description: state.description ?? `Shop order #${state.orderId}` };
  return null;
}

function formatCardNumber(raw: string): string {
  return raw.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiry(raw: string): string {
  const digits = raw.replace(/\D/g, "").slice(0, 4);
  return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
}

/** The standard check digit test every real card number passes. */
function passesLuhn(digits: string): boolean {
  let sum = 0;
  for (let i = 0; i < digits.length; i++) {
    let digit = Number(digits[digits.length - 1 - i]);
    if (i % 2 === 1) {
      digit *= 2;
      if (digit > 9) digit -= 9;
    }
    sum += digit;
  }
  return digits.length >= 13 && sum % 10 === 0;
}

function isExpired(expiry: string): boolean {
  const [month, year] = expiry.split("/").map(Number);
  const now = new Date();
  const fullYear = 2000 + year;
  return fullYear < now.getFullYear() || (fullYear === now.getFullYear() && month < now.getMonth() + 1);
}

function Payment() {
  const location = useLocation();
  const fromState = targetFromState((location.state ?? {}) as PaymentLocationState);
  const [chosen, setChosen] = useState<PaymentTarget | null>(null);
  const target = fromState ?? chosen;

  if (!target) return <ChoosePayment onChoose={setChosen} />;
  return <PayForm target={target} onBack={fromState ? undefined : () => setChosen(null)} />;
}

/** Opened from the dashboard with nothing selected: list what is still waiting to be paid. */
function ChoosePayment({ onChoose }: { onChoose: (target: PaymentTarget) => void }) {
  const studentId = getStudentId();
  const { data, loading, error, reload } = useFetch(async () => {
    try {
      const [bookings, orders] = await Promise.all([
        bookingService.getBookingsByStudent(studentId),
        orderService.getOrdersByStudent(studentId),
      ]);
      const unpaidBookings: PaymentTarget[] = bookings
        .filter((booking) => booking.status === "PENDING" && booking.totalAmount > 0)
        .map((booking) => ({ kind: "booking", id: booking.id, amount: booking.totalAmount, description: `Booking #${booking.id}, ${bookingDate(booking)}` }));
      const unpaidOrders: PaymentTarget[] = orders
        .filter((order) => order.status === "PENDING")
        .map((order) => ({ kind: "order", id: order.orderId, amount: order.totalAmount, description: `Shop order #${order.orderId}` }));
      return [...unpaidBookings, ...unpaidOrders];
    } catch (err) {
      throw new Error(friendlyError(err, "We couldn't load what you owe."));
    }
  }, studentId);

  const owed = data ?? [];

  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Payment</h1>
          <p>Bookings and orders that are waiting for payment.</p>
        </div>
      </header>

      {loading && <Loading message="Checking what you owe..." />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && owed.length === 0 && (
        <EmptyState
          title="Nothing to pay"
          message="You're all paid up. A booking with an added service, or a shop order, will show here until it's paid."
          action={<Link to="/dashboard" className="btn btn-primary">Back to home</Link>}
        />
      )}
      {!loading && !error && owed.length > 0 && (
        <section className="card">
          <ul className="list">
            {owed.map((item) => (
              <li key={`${item.kind}-${item.id}`}>
                <span>
                  <strong>{item.description}</strong>
                  <br />
                  <span className="muted small">{formatCurrency(item.amount)}</span>
                </span>
                <button type="button" className="btn btn-primary btn-sm" onClick={() => onChoose(item)}>
                  Pay
                </button>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

function PayForm({ target, onBack }: { target: PaymentTarget; onBack?: () => void }) {
  const toast = useToast();
  const reference = eftReference(target);
  const what = target.kind === "booking" ? "booking" : "order";

  // If an EFT was already submitted for this, don't take a second payment.
  const existing = useFetch(() => getPaymentsFor(target.kind, target.id).catch(() => []), `${target.kind}-${target.id}`);
  const awaiting = existing.data ? isAwaitingVerification(existing.data) : false;

  const [method, setMethod] = useState<Method>("card");
  const [cardNumber, setCardNumber] = useState("");
  const [cardName, setCardName] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [cvv, setCvv] = useState("");
  const [payerName, setPayerName] = useState("");
  const [payerBank, setPayerBank] = useState("");
  const [eftConfirmed, setEftConfirmed] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");
  const [result, setResult] = useState<PaymentRecord | null>(null);

  const errors: Record<string, string> = {};
  if (method === "card") {
    const digits = cardNumber.replace(/\s/g, "");
    if (!cardName.trim()) errors.cardName = "Enter the name on the card.";
    if (digits.length < 13) errors.cardNumber = "Enter the full card number.";
    else if (!passesLuhn(digits)) errors.cardNumber = "That card number isn't valid. Check the digits.";
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) errors.expiryDate = "Use the format MM/YY.";
    else if (isExpired(expiryDate)) errors.expiryDate = "This card has expired.";
    if (!/^\d{3,4}$/.test(cvv)) errors.cvv = "Enter the 3 digits on the back of the card.";
  } else {
    if (!payerName.trim()) errors.payerName = "Enter the name on the account you paid from.";
    if (!payerBank) errors.payerBank = "Choose the bank you paid from.";
    if (!eftConfirmed) errors.eftConfirmed = "Tick this once you've made the transfer.";
  }

  const copyReference = async () => {
    try {
      await navigator.clipboard.writeText(reference);
      toast.success("Reference copied.");
    } catch {
      toast.error(`Couldn't copy. The reference is ${reference}.`);
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitted(true);
    setServerError("");
    if (Object.keys(errors).length > 0) return;

    try {
      setSubmitting(true);
      // Card details are checked here but never sent: no payment gateway is connected.
      const payment =
        method === "card"
          ? await createPayment(target, "CARD")
          : await createPayment(target, "EFT", { reference, payerName: payerName.trim(), payerBank });
      setResult(payment);
    } catch (error) {
      setServerError(friendlyError(error, "We couldn't record your payment. Try again."));
    } finally {
      setSubmitting(false);
    }
  };

  const doneLinks = (
    <div className="btn-row">
      <Link to={target.kind === "booking" ? "/my-bookings" : "/order-items"} className="btn btn-primary">
        {target.kind === "booking" ? "View my bookings" : "View my orders"}
      </Link>
      <Link to="/dashboard" className="btn btn-ghost">Back to home</Link>
    </div>
  );

  // ---------- After paying ----------

  if (result && result.status?.toUpperCase() === "COMPLETED") {
    return (
      <div className="page narrow">
        <div className="card state">
          <span className="tick"><Icon name="check" size={36} /></span>
          <h1>Payment successful</h1>
          <p>
            {formatCurrency(result.amount)} paid by card.{" "}
            {target.kind === "booking" ? "Your booking is now confirmed." : "Your order is paid. Collect it from the laundry room."}
          </p>
          {doneLinks}
        </div>
      </div>
    );
  }

  if (result) {
    return (
      <div className="page narrow">
        <div className="card state">
          <h1>EFT submitted</h1>
          <p>
            We've noted your transfer of {formatCurrency(result.amount)} with reference <strong>{reference}</strong>. It is not confirmed yet:
            EFTs usually take {EFT_CLEARING_TIME} to reflect.
          </p>
          <p>
            {target.kind === "booking"
              ? "Your machine stays reserved in the meantime. The booking is confirmed once the money arrives, and you'll get a notification."
              : "Your items are held for you. The order is marked paid once the money arrives, and you'll get a notification."}
          </p>
          {doneLinks}
        </div>
      </div>
    );
  }

  // ---------- Before paying ----------

  if (existing.loading) {
    return <div className="page narrow"><Loading message="Loading payment..." /></div>;
  }

  if (awaiting) {
    return (
      <div className="page narrow">
        <div className="card state">
          <h1>EFT being verified</h1>
          <p>
            You've already submitted an EFT for this {what} (reference <strong>{reference}</strong>). We're waiting for the money to reflect,
            which usually takes {EFT_CLEARING_TIME}. You don't need to pay again.
          </p>
          {doneLinks}
        </div>
      </div>
    );
  }

  return (
    <div className="page narrow">
      {onBack && (
        <button type="button" className="back link-btn" onClick={onBack}>
          <Icon name="arrowLeft" size={18} /> Everything to pay
        </button>
      )}
      <header className="page-head">
        <div>
          <h1>Payment</h1>
          <p>Choose how you want to pay.</p>
        </div>
      </header>

      <section className="card row">
        <div>
          <span className="muted small">Amount due</span>
          <div className="price">{formatCurrency(target.amount)}</div>
        </div>
        <span className="muted">{target.description}</span>
      </section>

      <form className="card stack" onSubmit={handleSubmit} noValidate>
        <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
          <legend style={{ fontWeight: 600, marginBottom: 8, padding: 0 }}>Payment method</legend>
          <div className="choice-row">
            {(["card", "eft"] as const).map((option) => (
              <label key={option} className={`choice ${method === option ? "selected" : ""}`}>
                <input
                  type="radio"
                  name="method"
                  value={option}
                  checked={method === option}
                  onChange={() => {
                    setMethod(option);
                    setSubmitted(false);
                  }}
                />
                {option === "card" ? "Card" : "EFT (bank transfer)"}
              </label>
            ))}
          </div>
        </fieldset>

        {method === "card" ? (
          <>
            <div className="form-grid">
              <TextField
                className="span-2"
                id="cardName"
                label="Name on card"
                autoComplete="cc-name"
                value={cardName}
                onChange={setCardName}
                error={submitted ? errors.cardName : undefined}
              />
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
            <p className="muted small" style={{ margin: 0 }}>
              Demo checkout: no payment gateway is connected, so your card is not charged and card details are not sent anywhere.
            </p>
          </>
        ) : (
          <>
            <div className="alert alert-info stack" style={{ gap: 10 }}>
              <strong>1. Transfer {formatCurrency(target.amount)} from your banking app to:</strong>
              <dl className="dl" style={{ margin: 0 }}>
                <dt>Account name</dt>
                <dd>{EFT_ACCOUNT.accountName}</dd>
                <dt>Bank</dt>
                <dd>{EFT_ACCOUNT.bank}</dd>
                <dt>Account number</dt>
                <dd>{EFT_ACCOUNT.accountNumber}</dd>
                <dt>Branch code</dt>
                <dd>{EFT_ACCOUNT.branchCode}</dd>
                <dt>Account type</dt>
                <dd>{EFT_ACCOUNT.accountType}</dd>
                <dt>Reference</dt>
                <dd>
                  <strong>{reference}</strong>{" "}
                  <button type="button" className="link-btn small" onClick={copyReference} aria-label="Copy reference">
                    <Icon name="copy" size={16} /> Copy
                  </button>
                </dd>
              </dl>
              <span className="small">Use this exact reference, or we can't match your payment.</span>
            </div>

            <strong>2. Tell us who paid</strong>
            <div className="form-grid">
              <TextField
                className="span-2"
                id="payerName"
                label="Account holder's name"
                hint="As it appears on the account you paid from."
                autoComplete="name"
                value={payerName}
                onChange={setPayerName}
                error={submitted ? errors.payerName : undefined}
              />
              <div className="field span-2">
                <label htmlFor="payerBank">Bank you paid from</label>
                <select
                  id="payerBank"
                  className="input"
                  value={payerBank}
                  onChange={(event) => setPayerBank(event.target.value)}
                  aria-invalid={submitted && errors.payerBank ? true : undefined}
                  aria-describedby={submitted && errors.payerBank ? "payerBank-error" : undefined}
                >
                  <option value="">Choose your bank</option>
                  {SA_BANKS.map((bank) => (
                    <option key={bank} value={bank}>{bank}</option>
                  ))}
                </select>
                {submitted && errors.payerBank && <span id="payerBank-error" className="field-error">{errors.payerBank}</span>}
              </div>
            </div>

            <div className="field">
              <label style={{ display: "flex", gap: 10, alignItems: "flex-start", fontWeight: 400 }}>
                <input
                  type="checkbox"
                  checked={eftConfirmed}
                  onChange={(event) => setEftConfirmed(event.target.checked)}
                  style={{ marginTop: 4 }}
                  aria-describedby={submitted && errors.eftConfirmed ? "eftConfirmed-error" : undefined}
                />
                I've made the transfer of {formatCurrency(target.amount)} using the reference {reference}.
              </label>
              {submitted && errors.eftConfirmed && <span id="eftConfirmed-error" className="field-error">{errors.eftConfirmed}</span>}
            </div>

            <p className="muted small" style={{ margin: 0 }}>
              Your {what} stays {target.kind === "booking" ? "reserved" : "on hold"} until the money reflects, usually within {EFT_CLEARING_TIME}.
            </p>
          </>
        )}

        {serverError && <div className="alert alert-error" role="alert">{serverError}</div>}

        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? "Submitting..." : method === "card" ? `Pay ${formatCurrency(target.amount)}` : "Submit EFT for verification"}
        </button>
      </form>
    </div>
  );
}

export default Payment;
