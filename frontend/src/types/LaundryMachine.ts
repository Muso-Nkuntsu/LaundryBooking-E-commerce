export type MachineStatus = "AVAILABLE" | "IN_USE" | "OUT_OF_SERVICE";

export interface LaundryMachine {
    machineId?: number;
    machineNumber: string;
    type: string;
    status: MachineStatus;
    laundryRoomId: number;
}