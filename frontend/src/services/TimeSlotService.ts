import { apiGet } from "./Api";
import type { TimeSlot } from "../types/TimeSlot";

// Shape sent by the backend: a true/false flag instead of a status.
// The flag may arrive as "available" or "isAvailable" depending on the JSON settings.
interface RawTimeSlot {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  available?: boolean;
  isAvailable?: boolean;
}

const toTimeSlot = (raw: RawTimeSlot): TimeSlot => {
  // Treat a slot as available unless the backend clearly says it is not.
  const available = raw.available ?? raw.isAvailable ?? true;
  return {
    id: raw.id,
    date: String(raw.date).slice(0, 10),
    // "08:00:00" from the backend becomes "08:00".
    startTime: String(raw.startTime).slice(0, 5),
    endTime: String(raw.endTime).slice(0, 5),
    status: available ? "AVAILABLE" : "UNAVAILABLE",
  };
};

export const timeSlotService = {
  async getUpcomingTimeSlots(days = 7): Promise<TimeSlot[]> {
    return (await apiGet<RawTimeSlot[]>("/timeslot/upcoming", { days })).map(toTimeSlot);
  },

  async getTimeSlotsByDate(date: string): Promise<TimeSlot[]> {
    return (await apiGet<RawTimeSlot[]>(`/timeslot/date/${date}`)).map(toTimeSlot);
  },

  async getTimeSlotById(id: number): Promise<TimeSlot> {
    return toTimeSlot(await apiGet<RawTimeSlot>(`/timeslot/${id}`));
  },
};