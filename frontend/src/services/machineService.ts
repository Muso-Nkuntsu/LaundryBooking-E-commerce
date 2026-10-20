import type { LaundryMachine, MachineStatus } from "../types/LaundryMachine";
import { ApiError } from "./Api";

const MACHINE_BASE_URL: string =
    (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api").replace(/\/api$/, "") +
    "/laundrymachine";

const handle = async <T>(response: Response): Promise<T> => {
    if (!response.ok) {
        throw new ApiError(`Request failed with status ${response.status}`, response.status);
    }
    if (response.status === 204) {
        return undefined as T;
    }
    return (await response.json()) as T;
};

export const machineService = {
    getAllMachines(): Promise<LaundryMachine[]> {
        return fetch(`${MACHINE_BASE_URL}/getall`).then((res) => handle<LaundryMachine[]>(res));
    },

    getMachineById(machineId: number): Promise<LaundryMachine> {
        return fetch(`${MACHINE_BASE_URL}/read/${machineId}`).then((res) => handle<LaundryMachine>(res));
    },

    getMachinesByStatus(status: MachineStatus): Promise<LaundryMachine[]> {
        return fetch(`${MACHINE_BASE_URL}/status/${status}`).then((res) => handle<LaundryMachine[]>(res));
    },

    async getMachinesByRoom(roomId: number): Promise<LaundryMachine[]> {
        const machines = await this.getAllMachines();
        return machines.filter((machine) => machine.laundryRoomId === roomId);
    },
};