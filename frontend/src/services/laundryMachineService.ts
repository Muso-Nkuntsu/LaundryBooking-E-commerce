import { apiGet } from "./Api";
import type { LaundryMachine } from "../types/booking";

export const getAllLaundryMachines = (): Promise<LaundryMachine[]> =>
  apiGet<LaundryMachine[]>("/laundrymachine/getall");
