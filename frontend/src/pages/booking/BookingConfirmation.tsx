import { Link, useLocation } from "react-router-dom";
import type { Booking } from "../../types/booking";
import { EmptyState } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { STATUS_LABEL, STATUS_PILL, bookingDate, bookingTime } from "../../utilis/bookingDisplay";

function BookingConfirmation() {
  const location = useLocation();
  const booking = (location.state as { booking?: Booking } | null)?.booking;

  // Prevent direct access without a booking
  if (!booking) {
    return (
      <div className="page narrow">
        <EmptyState
          title="No booking to show"
          message="This page appears right after you book. Start a new booking to see it."
          action={<Link to="/make-booking" className="btn btn-primary">Book a machine</Link>}
        />
      </div>
    );
  }

  const machine = booking.laundryMachine;
  const room = machine?.laundryRoom;

  return (
    <div className="page narrow">
      <div className="state" style={{ paddingTop: 8, paddingBottom: 24 }}>
        <span className="tick"><Icon name="check" size={36} /></span>
        <h1>You're booked</h1>
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
          <dt>Status</dt>
          <dd><span className={STATUS_PILL[booking.status] ?? "pill pill-off"} style={{ marginLeft: "auto" }}>{STATUS_LABEL[booking.status] ?? booking.status}</span></dd>
          <dt>Total</dt>
          <dd>{formatCurrency(booking.totalAmount ?? 0)}</dd>
        </dl>
      </section>

      <div className="btn-row" style={{ marginTop: 18 }}>
        <Link to="/my-bookings" className="btn btn-primary">View my bookings</Link>
        {booking.totalAmount > 0 && (
          <Link to="/payment" state={{ amount: booking.totalAmount, description: `Booking #${booking.id}`, bookingId: booking.id }} className="btn btn-sun">
            Pay now
          </Link>
        )}
        <Link to="/make-booking" className="btn btn-ghost">Book another</Link>
      </div>
    </div>
  );
}

export default BookingConfirmation;
