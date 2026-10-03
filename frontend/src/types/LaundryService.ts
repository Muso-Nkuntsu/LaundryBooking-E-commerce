export interface LaundryService {
  id: number;
  name: string;
  description: string;
  price: number;
  isAvailable: boolean;
  durationMinutes?: number;
}