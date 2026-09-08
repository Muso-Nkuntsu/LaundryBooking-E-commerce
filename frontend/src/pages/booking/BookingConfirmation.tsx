import { useLocation, useNavigate } from "react-router-dom";
import type { Booking } from "../../types/booking";

function BookingConfirmation() {
  const location = useLocation();
  const navigate = useNavigate();

  const booking = location.state?.booking as Booking | undefined;

  // Prevent direct access without a booking
  if (!booking) {
    return (
      <div>
        <h1>No Booking Found</h1>

        <p>
          There is no booking information available.
        </p>

        <button onClick={() => navigate("/make-booking")}>
          Make a Booking
        </button>
      </div>
    );
  }

  const room = booking.laundryMachine?.laundryRoom;
  const machine = booking.laundryMachine;
  const timeSlot = booking.timeSlot;

  return (
    <div>
      <h1>Booking Confirmed!</h1>

      <p>
        Your laundry booking has been successfully created.
      </p>

      <hr />

      <h2>Booking Details</h2>

      <p>
        <strong>Booking ID:</strong> {booking.id}
      </p>

      <p>
        <strong>Laundry Room:</strong>{" "}
        {room
          ? `${room.roomNumber} - ${room.location}`
          : "Not available"}
      </p>

      <p>
        <strong>Machine:</strong>{" "}
        {machine
          ? `${machine.machineNumber} (${machine.type})`
          : "Not available"}
      </p>

      <p>
        <strong>Date:</strong>{" "}
        {timeSlot?.date || "Not available"}
      </p>

      <p>
        <strong>Time Slot:</strong>{" "}
        {timeSlot
          ? `${timeSlot.startTime} - ${timeSlot.endTime}`
          : "Not available"}
      </p>

      <p>
        <strong>Status:</strong>{" "}
        {booking.status}
      </p>

      <p>
        <strong>Total Amount:</strong>{" "}
        R{booking.totalAmount.toFixed(2)}
      </p>

      <hr />

      <button onClick={() => navigate("/my-bookings")}>
        View My Bookings
      </button>

      <button onClick={() => navigate("/make-booking")}>
        Make Another Booking
      </button>
    </div>
  );
}

export default BookingConfirmation;