import { apiGet } from "./api";
import type { LaundryService } from "../types/LaundryService";


export const laundryServiceService = {
 
  getAllServices(): Promise<LaundryService[]> {
    return apiGet<LaundryService[]>("/laundry-services");
  },

  getServiceById(id: number): Promise<LaundryService> {
    return apiGet<LaundryService>(`/laundry-services/${id}`);
  },
};