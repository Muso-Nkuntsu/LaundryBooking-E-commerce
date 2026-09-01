import api from "./api";
import { LaundryService } from "../types/LaundryService";


export const laundryServiceService = {

  async getAllServices(): Promise<LaundryService[]> {
    const response = await api.get<LaundryService[]>("/laundry-services");
    return response.data;
  },

  async getServiceById(id: number): Promise<LaundryService> {
    const response = await api.get<LaundryService>(`/laundry-services/${id}`);
    return response.data;
  },
};