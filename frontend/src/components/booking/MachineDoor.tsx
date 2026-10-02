import Drum from "../common/Drum";
import type { LaundryMachine } from "../../types/booking";
import { isMachineAvailable } from "../../utilis/machine";

function statusText(status: string): string {
  const text = (status || "Unknown").replace(/_/g, " ").toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

interface MachineDoorProps {
  machine: LaundryMachine;
  selected?: boolean;
  /** Makes the door a button. */
  onSelect?: (machine: LaundryMachine) => void;
  /** When choosing a machine for a time slot: whether it can be booked for that slot. */
  bookable?: boolean;
  /** Shown in place of the status when the machine can't be booked for the slot. */
  unavailableLabel?: string;
}

/** One machine, drawn as its door. */
function MachineDoor({ machine, selected = false, onSelect, bookable, unavailableLabel }: MachineDoorProps) {
  const outOfOrder = machine.status === "OUT_OF_ORDER";
  const inUse = machine.status === "IN_USE";

  // On the booking screen "free" means free for the chosen slot; elsewhere it means free right now.
  const choosing = bookable !== undefined;
  const free = choosing ? bookable : isMachineAvailable(machine);
  const label = choosing ? (free ? "Free" : unavailableLabel ?? statusText(machine.status)) : statusText(machine.status);
  const pill = free ? "pill-ok" : outOfOrder ? "pill-off" : "pill-warn";
  const drumState = outOfOrder ? "off" : !choosing && inUse ? "spinning" : !free && choosing ? "off" : "idle";

  const body = (
    <>
      <Drum state={drumState} />
      <strong>{machine.machineNumber}</strong>
      <span className="muted small">{machine.type}</span>
      {(!choosing || unavailableLabel !== undefined || free) && <span className={`pill ${pill}`}>{label}</span>}
    </>
  );

  if (!onSelect) {
    return <div className={`machine ${free ? "" : "off"}`}>{body}</div>;
  }

  return (
    <button
      type="button"
      className={`machine ${selected ? "selected" : ""}`}
      disabled={!free}
      aria-pressed={selected}
      onClick={() => onSelect(machine)}
    >
      {body}
    </button>
  );
}

export default MachineDoor;
