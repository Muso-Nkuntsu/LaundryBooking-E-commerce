import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { bookingService } from "../../services/bookingService";

import type {
  LaundryRoom,
  LaundryMachine,
  TimeSlot,
} from "../../types/booking";

function MakeBooking() {
  const navigate = useNavigate();

  const [rooms, setRooms] = useState<LaundryRoom[]>([]);
  const [machines, setMachines] = useState<LaundryMachine[]>([]);
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);

  const [selectedRoom, setSelectedRoom] =
    useState<LaundryRoom | null>(null);

  const [selectedMachine, setSelectedMachine] =
    useState<LaundryMachine | null>(null);

  const [selectedTimeSlot, setSelectedTimeSlot] =
    useState<TimeSlot | null>(null);

  const [loading, setLoading] = useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);


  // Load rooms, machines and time slots

  useEffect(() => {
    const loadBookingData = async () => {
      try {
        setLoading(true);

        const [
          roomsData,
          machinesData,
          timeSlotsData,
        ] = await Promise.all([
          bookingService.getActiveRooms(),
          bookingService.getAllMachines(),
          bookingService.getAllTimeSlots(),
        ]);

        setRooms(roomsData);
        setMachines(machinesData);
        setTimeSlots(timeSlotsData);

      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load booking data"
        );
      } finally {
        setLoading(false);
      }
    };

    loadBookingData();
  }, []);


  // Filter machines based on selected room

  const filteredMachines = selectedRoom
    ? machines.filter(
        (machine) =>
          machine.laundryRoom?.roomId ===
          selectedRoom.roomId
      )
    : [];


  // Filter available time slots

  const availableTimeSlots =
    timeSlots.filter(
      (slot) =>
        slot.isAvailable === true ||
        slot.available === true
    );


  // Handle room selection

  const handleRoomChange = (
    roomId: number
  ) => {
    const room = rooms.find(
      (room) => room.roomId === roomId
    );

    setSelectedRoom(room || null);

    // Reset machine when room changes
    setSelectedMachine(null);
  };


  // Handle machine selection

  const handleMachineChange = (
    machineId: number
  ) => {
    const machine = machines.find(
      (machine) =>
        machine.machineId === machineId
    );

    setSelectedMachine(machine || null);
  };


  // Handle time slot selection

  const handleTimeSlotChange = (
    timeSlotId: number
  ) => {
    const timeSlot = timeSlots.find(
      (slot) => slot.id === timeSlotId
    );

    setSelectedTimeSlot(timeSlot || null);
  };


  // Confirm booking

  const handleConfirmBooking = async () => {

    if (
      !selectedMachine ||
      !selectedTimeSlot
    ) {
      setError(
        "Please select a room, machine and time slot."
      );
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      /*
       TEMPORARY STUDENT ID

       Later this should come from the
       logged-in student authentication.
      */

      const studentId = 1;

      const booking =
        await bookingService.createBooking({
          studentId: studentId,
          machineId:
            selectedMachine.machineId,
          timeSlotId:
            selectedTimeSlot.id,
          totalAmount: 50,
        });

      // Navigate to confirmation page
      navigate("/booking-confirmation", {
        state: {
          booking,
        },
      });

    } catch (error) {

      setError(
        error instanceof Error
          ? error.message
          : "Failed to create booking"
      );

    } finally {
      setSubmitting(false);
    }
  };


  if (loading) {
    return (
      <div>
        <h2>Make a Booking</h2>
        <p>Loading booking information...</p>
      </div>
    );
  }


  return (
    <div>

      <h1>Make a Booking</h1>

      {error && (
        <p>
          {error}
        </p>
      )}


      {/* SELECT ROOM */}

      <div>

        <h3>1. Select Laundry Room</h3>

        <select
          value={
            selectedRoom?.roomId || ""
          }
          onChange={(event) =>
            handleRoomChange(
              Number(event.target.value)
            )
          }
        >

          <option value="">
            Select a laundry room
          </option>

          {rooms.map((room) => (
            <option
              key={room.roomId}
              value={room.roomId}
            >
              {room.roomNumber} - {room.location}
            </option>
          ))}

        </select>

      </div>


      {/* SELECT MACHINE */}

      <div>

        <h3>2. Select Machine</h3>

        <select
          value={
            selectedMachine?.machineId || ""
          }
          disabled={!selectedRoom}
          onChange={(event) =>
            handleMachineChange(
              Number(event.target.value)
            )
          }
        >

          <option value="">
            Select a machine
          </option>

          {filteredMachines.map(
            (machine) => (

              <option
                key={machine.machineId}
                value={machine.machineId}
              >
                {machine.machineNumber}
                {" - "}
                {machine.type}
                {" ("}
                {machine.status}
                {")"}
              </option>

            )
          )}

        </select>

      </div>


      {/* SELECT TIME SLOT */}

      <div>

        <h3>3. Select Time Slot</h3>

        <select
          value={
            selectedTimeSlot?.id || ""
          }
          onChange={(event) =>
            handleTimeSlotChange(
              Number(event.target.value)
            )
          }
        >

          <option value="">
            Select a time slot
          </option>

          {availableTimeSlots.map(
            (slot) => (

              <option
                key={slot.id}
                value={slot.id}
              >
                {slot.date}
                {" | "}
                {slot.startTime}
                {" - "}
                {slot.endTime}
              </option>

            )
          )}

        </select>

      </div>


      {/* BOOKING REVIEW */}

      <div>

        <h2>Booking Review</h2>

        <p>
          <strong>Room:</strong>{" "}
          {selectedRoom
            ? `${selectedRoom.roomNumber} - ${selectedRoom.location}`
            : "Not selected"}
        </p>

        <p>
          <strong>Machine:</strong>{" "}
          {selectedMachine
            ? selectedMachine.machineNumber
            : "Not selected"}
        </p>

        <p>
          <strong>Date:</strong>{" "}
          {selectedTimeSlot
            ? selectedTimeSlot.date
            : "Not selected"}
        </p>

        <p>
          <strong>Time:</strong>{" "}
          {selectedTimeSlot
            ? `${selectedTimeSlot.startTime} - ${selectedTimeSlot.endTime}`
            : "Not selected"}
        </p>

        <p>
          <strong>Total:</strong> R50.00
        </p>

      </div>


      {/* CONFIRM */}

      <button
        onClick={handleConfirmBooking}
        disabled={
          !selectedRoom ||
          !selectedMachine ||
          !selectedTimeSlot ||
          submitting
        }
      >
        {submitting
          ? "Confirming Booking..."
          : "Confirm Booking"}
      </button>

    </div>
  );
}

export default MakeBooking;