import type { LaundryMachine } from "../types/booking";

export function isMachineAvailable(machine: LaundryMachine): boolean {
  return machine.status?.toUpperCase() === "AVAILABLE";
}
