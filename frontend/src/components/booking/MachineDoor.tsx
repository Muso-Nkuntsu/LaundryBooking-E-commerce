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
  onSelect?: (machine: LaundryMachine) => void;
}

/** One machine, drawn as its door. Clickable when `onSelect` is given. */
function MachineDoor({ machine, selected = false, onSelect }: MachineDoorProps) {
  const available = isMachineAvailable(machine);
  const inUse = /USE|BUSY|RUNNING|BOOKED/i.test(machine.status ?? "");
  const drum = <Drum state={available ? "idle" : inUse ? "spinning" : "off"} />;

  const body = (
    <>
      {drum}
      <strong>{machine.machineNumber}</strong>
      <span className="muted small">{machine.type}</span>
      <span className={`pill ${available ? "pill-ok" : inUse ? "pill-warn" : "pill-off"}`}>{statusText(machine.status)}</span>
    </>
  );

  if (!onSelect) {
    return <div className={`machine ${available ? "" : "off"}`}>{body}</div>;
  }

  return (
    <button
      type="button"
      className={`machine ${selected ? "selected" : ""}`}
      disabled={!available}
      aria-pressed={selected}
      onClick={() => onSelect(machine)}
    >
      {body}
    </button>
  );
}

export default MachineDoor;
