import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { bookingService } from "../../services/bookingService";
import type { Booking } from "../../types/booking";

function MyBookings() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Temporary student ID
  const studentId = 1;


  const loadBookings = async () => {
    try {
      setLoading(true);
      setError(null);

      const data =
        await bookingService.getBookingsByStudent(studentId);

      setBookings(data);

    } catch (error) {

      setError(
        error instanceof Error
          ? error.message
          : "Failed to load bookings"
      );

    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    loadBookings();
  }, []);


  const handleCancelBooking = async (
    bookingId: number
  ) => {

    const confirmed = window.confirm(
      "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) return;

    try {

      await bookingService.cancelBooking(bookingId);

      // Reload bookings after cancellation
      await loadBookings();

    } catch (error) {

      setError(
        error instanceof Error
          ? error.message
          : "Failed to cancel booking"
      );
    }
  };


  if (loading) {
    return (
      <div>
        <h1>My Bookings</h1>
        <p>Loading bookings...</p>
      </div>
    );
  }


  return (
    <div>

      <h1>My Bookings</h1>

      <button onClick={() => navigate("/make-booking")}>
        Make a New Booking
      </button>


      {error && (
        <p>
          {error}
        </p>
      )}


      {bookings.length === 0 ? (

        <p>You currently have no bookings.</p>

      ) : (

        <div>

          {bookings.map((booking) => {

            const room =
              booking.laundryMachine?.laundryRoom;

            const machine =
              booking.laundryMachine;

            const timeSlot =
              booking.timeSlot;

            return (

              <div
                key={booking.id}
                style={{
                  border: "1px solid #ccc",
                  padding: "15px",
                  marginTop: "15px",
                }}
              >

                <h3>
                  Booking #{booking.id}
                </h3>

                <p>
                  <strong>Status:</strong>{" "}
                  {booking.status}
                </p>

                <p>
                  <strong>Room:</strong>{" "}
                  {room
                    ? `${room.roomNumber} - ${room.location}`
                    : "Not available"}
                </p>

                <p>
                  <strong>Machine:</strong>{" "}
                  {machine?.machineNumber ||
                    "Not available"}
                </p>

                <p>
                  <strong>Date:</strong>{" "}
                  {timeSlot?.date ||
                    "Not available"}
                </p>

                <p>
                  <strong>Time:</strong>{" "}
                  {timeSlot
                    ? `${timeSlot.startTime} - ${timeSlot.endTime}`
                    : "Not available"}
                </p>

                <p>
                  <strong>Total:</strong>{" "}
                  R{booking.totalAmount.toFixed(2)}
                </p>


                {/* Only allow cancellation if confirmed */}

                {booking.status === "CONFIRMED" && (

                  <button
                    onClick={() =>
                      handleCancelBooking(
                        booking.id
                      )
                    }
                  >
                    Cancel Booking
                  </button>

                )}

              </div>
            );
          })}

        </div>
      )}

    </div>
  );
}

export default MyBookings;