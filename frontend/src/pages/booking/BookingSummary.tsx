import { Link, useLocation } from "react-router-dom";
import type { Booking } from "../../types/booking";
import { EmptyState } from "../../components/common/States";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { bookingDate, bookingTime } from "../../utilis/bookingDisplay";

function BookingSummary() {
  const location = useLocation();
  const state = (location.state ?? {}) as {
  booking?: Booking;
  laundryServiceName?: string;
  laundryServicePrice?: number;
};

const booking = state.booking;
const serviceName = state.laundryServiceName ?? booking?.laundryService?.serviceName ?? "Not available";
const servicePrice = state.laundryServicePrice ?? booking?.laundryService?.price ?? booking?.totalAmount ?? 0;

  if (!booking) {
    return (
      <div className="page narrow">
        <EmptyState
          title="No booking to review"
          message="Please select a laundry service and create a booking first."
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
      <header className="page-head">
        <div>
          <h1>Booking Summary</h1>
          <p>Please review your booking details before proceeding to payment.</p>
        </div>
      </header>

      <section className="card">
        <dl className="dl">
          <dt>Booking number</dt>
          <dd>#{booking.id}</dd>

          <dt>Laundry service</dt>
          <dd>{serviceName}</dd>

          <dt>Laundry room</dt>
          <dd>
            {room
              ? `${room.roomNumber}, ${room.location}`
              : "Not available"}
          </dd>

          <dt>Machine</dt>
          <dd>
            {machine
              ? `${machine.machineNumber} (${machine.type})`
              : "Not available"}
          </dd>

          <dt>Booking date</dt>
          <dd>{bookingDate(booking)}</dd>

          <dt>Time slot</dt>
          <dd>{bookingTime(booking)}</dd>

          <dt>Service price</dt>
          <dd>{formatCurrency(servicePrice)}</dd>

          <dt>Total amount</dt>
          <dd>
            <strong>{formatCurrency(booking.totalAmount)}</strong>
          </dd>
        </dl>
      </section>

      <div className="btn-row" style={{ marginTop: 18 }}>
        <Link to="/my-bookings" className="btn btn-ghost">
          My Bookings
        </Link>

        <Link
          to="/payment"
          state={{
            amount: booking.totalAmount,
            description: `Booking #${booking.id}`,
            bookingId: booking.id,
            booking,
          }}
          className="btn btn-primary"
        >
          Proceed to Payment
        </Link>
      </div>
    </div>
  );
}

export default BookingSummary;