import type { LaundryMachine } from "../types/booking";

const API_URL = "http://localhost:8080/laundrymachine";

export const getAllLaundryMachines = async (): Promise<LaundryMachine[]> => {
  const response = await fetch(`${API_URL}/getall`);
  if (!response.ok) {
    throw new Error(`Failed to load laundry machines (${response.status})`);
  }
  return response.json();
};
