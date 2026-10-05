import { useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import type { TimeSlot } from "../../types/TimeSlot";
import type { LaundryMachine } from "../../types/booking";
import TimeSlotSelector from "../../components/booking/TimeSlotSelector";
import MachineDoor from "../../components/booking/MachineDoor";
import { Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { bookingService } from "../../services/bookingService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { formatFullDate, formatTimeRange } from "../../utilis/FormatDate";

interface MakeBookingLocationState {
  laundryServiceId?: number;
  laundryServiceName?: string;
  laundryServicePrice?: number;
}

const STEPS = ["Pick a time", "Pick a machine", "Confirm"];

function MakeBooking() {
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const [searchParams] = useSearchParams();

  const { laundryServiceId, laundryServiceName, laundryServicePrice } = (location.state as MakeBookingLocationState | null) ?? {};
  const roomFilter = Number(searchParams.get("roomId")) || null;

  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [selectedMachine, setSelectedMachine] = useState<LaundryMachine | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const machinesQuery = useFetch(() =>
    bookingService.getAllMachines().catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load the machines."));
    }),
  );
  const allMachines = machinesQuery.data ?? [];
  const machines = roomFilter ? allMachines.filter((machine) => machine.laundryRoom?.roomId === roomFilter) : allMachines;

  // Once a time is chosen, ask the backend which machines are still free for it.
  const slotId = selectedSlot?.id ?? 0;
  const freeQuery = useFetch(
    () => (slotId ? bookingService.getAvailableMachines(slotId) : Promise.resolve(null)),
    slotId,
  );
  const freeIds = freeQuery.data ? new Set(freeQuery.data.map((machine) => machine.machineId)) : null;
  const isFree = (machine: LaundryMachine) =>
    freeIds ? freeIds.has(machine.machineId) : machine.status !== "OUT_OF_ORDER";

  const total = laundryServicePrice ?? 0;
  const step = !selectedSlot ? 0 : !selectedMachine ? 1 : 2;

  const handleSlotSelect = (slot: TimeSlot) => {
    setSelectedSlot(slot);
    // A machine picked for another time may be taken at this one, so choose again.
    setSelectedMachine(null);
  };

  const handleConfirm = async () => {
    if (!selectedSlot || !selectedMachine) return;

    try {
      setSubmitting(true);
      const booking = await bookingService.createBooking({
        studentId: getStudentId(),
        machineId: selectedMachine.machineId,
        timeSlotId: selectedSlot.id,
        serviceId: laundryServiceId,
      });
      navigate("/booking-confirmation", { state: { booking } });
    } catch (error) {
      toast.error(friendlyError(error, "We couldn't create your booking. Try again."));
      // Someone may have taken the machine in the meantime, so refresh what is free.
      setSelectedMachine(null);
      freeQuery.reload();
    } finally {
      setSubmitting(false);
    }
  };

  const summary = !selectedSlot
    ? "Choose a time slot to start."
    : `${formatFullDate(selectedSlot.date)}, ${formatTimeRange(selectedSlot.startTime, selectedSlot.endTime)}${
        selectedMachine ? `, machine ${selectedMachine.machineNumber}` : ". Now choose a machine."
      }`;

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>Book a machine</h1>
          <p>Choose when you want to wash, then which machine.</p>
        </div>
      </header>

      <ol className="steps" aria-label="Booking steps">
        {STEPS.map((label, index) => (
          <li key={label} className={index === step ? "current" : index < step ? "done" : undefined} aria-current={index === step ? "step" : undefined}>
            <span>{index + 1}</span>
            {label}
          </li>
        ))}
      </ol>

      {laundryServiceName && (
        <div className="alert alert-info" style={{ marginBottom: 16 }}>
          Added service: <strong>{laundryServiceName}</strong>
          {typeof laundryServicePrice === "number" && ` (${formatCurrency(laundryServicePrice)})`}
        </div>
      )}

      <section className="card">
        <h2 style={{ marginBottom: 14 }}>When</h2>
        <TimeSlotSelector onSlotSelect={handleSlotSelect} selectedSlotId={selectedSlot?.id ?? null} />
      </section>

      <section className="card">
        <h2>Which machine</h2>
        <p className="muted small" style={{ margin: "4px 0 16px" }}>
          {!selectedSlot
            ? "Choose a time first to see which machines are free."
            : roomFilter
              ? "Machines in the room you chose that are free at this time."
              : "Machines that are free at this time, from every laundry room."}
        </p>

        {machinesQuery.loading && <Loading message="Loading machines..." />}
        {!machinesQuery.loading && machinesQuery.error && (
          <div className="alert alert-error row" role="alert">
            {machinesQuery.error}
            <button type="button" className="link-btn" onClick={machinesQuery.reload}>Try again</button>
          </div>
        )}
        {!machinesQuery.loading && !machinesQuery.error && machines.length === 0 && (
          <p className="muted">No machines found{roomFilter ? " in this room" : ""}.</p>
        )}
        {machines.length > 0 && (
          <div className="machines">
            {machines.map((machine) => (
              <MachineDoor
                key={machine.machineId}
                machine={machine}
                selected={selectedMachine?.machineId === machine.machineId}
                bookable={Boolean(selectedSlot) && isFree(machine)}
                unavailableLabel={!selectedSlot ? undefined : machine.status === "OUT_OF_ORDER" ? "Out of order" : "Booked"}
                onSelect={setSelectedMachine}
              />
            ))}
          </div>
        )}
      </section>

      <div className="sticky-bar">
        <span className="small" aria-live="polite">
          {summary}
          {total > 0 && <strong> Total {formatCurrency(total)}.</strong>}
        </span>
        <button type="button" className="btn btn-primary" disabled={!selectedSlot || !selectedMachine || submitting} onClick={handleConfirm}>
          {submitting ? "Booking..." : "Confirm booking"}
        </button>
      </div>
    </div>
  );
}

export default MakeBooking;
