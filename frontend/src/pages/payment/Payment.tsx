import { useState } from "react";
import type { FormEvent} from "react";

function Payment() {
  const [paymentMethod, setPaymentMethod] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [cvv, setCvv] = useState("");
  const [message, setMessage] = useState("");
  const [success, setSuccess] = useState(false);

  const amount = 0.00;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setMessage("");
    setSuccess(false);

    if (!paymentMethod) {
      setMessage("Please select a payment method.");
      return;
    }

    if (!cardNumber || !expiryDate || !cvv) {
      setMessage("Please complete all payment information.");
      return;
    }

    setSuccess(true);
    setMessage("Payment submitted successfully.");
  };

  return (
    <div>
      <h1>Payment</h1>

      <section>
        <h2>Order Summary</h2>
        <p>Amount Due: R{amount.toFixed(2)}</p>
      </section>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="paymentMethod">Payment Method</label>
          <br />
          <select
            id="paymentMethod"
            value={paymentMethod}
            onChange={(event) => setPaymentMethod(event.target.value)}
          >
            <option value="">Select payment method</option>
            <option value="card">Card</option>
            <option value="eft">EFT</option>
          </select>
        </div>

        <br />

        <div>
          <label htmlFor="cardNumber">Card Number</label>
          <br />
          <input
            id="cardNumber"
            type="text"
            value={cardNumber}
            onChange={(event) => setCardNumber(event.target.value)}
            placeholder="Enter card number"
          />
        </div>

        <br />

        <div>
          <label htmlFor="expiryDate">Expiry Date</label>
          <br />
          <input
            id="expiryDate"
            type="text"
            value={expiryDate}
            onChange={(event) => setExpiryDate(event.target.value)}
            placeholder="MM/YY"
          />
        </div>

        <br />

        <div>
          <label htmlFor="cvv">CVV</label>
          <br />
          <input
            id="cvv"
            type="password"
            value={cvv}
            onChange={(event) => setCvv(event.target.value)}
            placeholder="Enter CVV"
          />
        </div>

        <br />

        {message && <p>{message}</p>}

        <button type="submit">
          Submit Payment
        </button>
      </form>

      {success && (
        <section>
          <h2>Payment Successful</h2>
          <p>Your payment has been submitted successfully.</p>
        </section>
      )}
    </div>
  );
}

export default Payment;