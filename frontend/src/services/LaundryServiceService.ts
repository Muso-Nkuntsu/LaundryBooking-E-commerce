import { apiGet } from "./Api";
import type { LaundryService } from "../types/LaundryService";

// Shape sent by the backend: the name is "serviceName" and there is no availability flag.
interface RawLaundryService {
  id: number;
  serviceName: string;
  description: string;
  price: number;
}

const toService = (raw: RawLaundryService): LaundryService => ({
  id: raw.id,
  name: raw.serviceName,
  description: raw.description ?? "",
  price: raw.price,
  // Every service the backend lists can be booked.
  isAvailable: true,
});

export const laundryServiceService = {
  async getAllServices(): Promise<LaundryService[]> {
    return (await apiGet<RawLaundryService[]>("/laundry-service/getall")).map(toService);
  },

  async getServiceById(id: number): Promise<LaundryService> {
    return toService(await apiGet<RawLaundryService>(`/laundry-service/read/${id}`));
  },
};
