import { apiGet } from "./api";
import type { TimeSlot } from "../types/TimeSlot";

export const timeSlotService = {
  
  getUpcomingTimeSlots(days = 7): Promise<TimeSlot[]> {
    return apiGet<TimeSlot[]>("/timeslots/upcoming", { days });
  },

  getTimeSlotsByDate(date: string): Promise<TimeSlot[]> {
    return apiGet<TimeSlot[]>("/timeslots", { date });
  },

  
  getTimeSlotById(id: number): Promise<TimeSlot> {
    return apiGet<TimeSlot>(`/timeslots/${id}`);
  },
};