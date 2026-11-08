
import { Link, useLocation } from "react-router-dom";
import type { Booking } from "../../types/booking";
import type { PaymentRecord } from "../../services/paymentService";
import { EmptyState } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { STATUS_LABEL, STATUS_PILL, bookingDate, bookingTime } from "../../utilis/bookingDisplay";

function BookingConfirmation() {
  const location = useLocation();
  const state = (location.state ?? {}) as {
    booking?: Booking;
    payment?: PaymentRecord | null;
  };

  const booking = state.booking;
  const payment = state.payment;

  if (!booking) {
    return (
      <div className="page narrow">
        <EmptyState
          title="No booking to show"
          message="Start a new booking to see your booking details."
          action={
            <Link to="/laundry" className="btn btn-primary">
              Browse Services
            </Link>
          }
        />
      </div>
    );
  }

  const machine = booking.laundryMachine;
  const room = machine?.laundryRoom;

  return (
    <div className="page narrow">
      <div className="state" style={{ paddingTop: 8, paddingBottom: 24 }}>
        <span className="tick">
          <Icon name="check" size={36} />
        </span>
        <h1>Booking submitted</h1>
        <p>{bookingDate(booking)}, {bookingTime(booking)}</p>
      </div>

      <section className="card">
        <dl className="dl">
          <dt>Booking number</dt>
          <dd>#{booking.id}</dd>

          <dt>Laundry room</dt>
          <dd>{room ? `${room.roomNumber}, ${room.location}` : "Not set"}</dd>

          <dt>Machine</dt>
          <dd>{machine ? `${machine.machineNumber} (${machine.type})` : "Not set"}</dd>

          <dt>Booking status</dt>
          <dd>
            <span className={STATUS_PILL[booking.status] ?? "pill pill-off"}>
              {STATUS_LABEL[booking.status] ?? booking.status}
            </span>
          </dd>

          <dt>Payment status</dt>
          <dd>{payment?.status ?? "Not submitted"}</dd>

          <dt>Total</dt>
          <dd><strong>{formatCurrency(booking.totalAmount ?? 0)}</strong></dd>
        </dl>
      </section>

      <div className="btn-row" style={{ marginTop: 18 }}>
        <Link to="/my-bookings" className="btn btn-primary">
          View my bookings
        </Link>
        <Link to="/dashboard" className="btn btn-ghost">
          Back to Dashboard
        </Link>
      </div>
    </div>
  );
}

export default BookingConfirmation;
