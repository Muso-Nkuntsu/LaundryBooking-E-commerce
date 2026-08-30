import { useNavigate } from "react-router-dom";
import TimeSlotSelection from "../../components/booking/TimeSlotSelection";
import { useBooking } from "../../context/useBooking";
import type { TimeSlot } from "../../context/BookingContext";

export default function BookingPage() {
  const { residenceId, setSelectedSlot } = useBooking();
  const navigate = useNavigate();

  function handleSlotSelected(slot: TimeSlot) {
    setSelectedSlot(slot);
    navigate("/booking/confirm");
  }

  return (
    <TimeSlotSelection
      residenceId={residenceId}
      onSlotSelected={handleSlotSelected}
    />
  );
}