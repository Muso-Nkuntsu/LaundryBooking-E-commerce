import api from "./api";
import { TimeSlot } from "../types/TimeSlot";

export const timeSlotService = {

  async getUpcomingTimeSlots(days = 7): Promise<TimeSlot[]> {
    const response = await api.get<TimeSlot[]>("/timeslots/upcoming", {
      params: { days },
    });
    return response.data;
  },

  async getTimeSlotsByDate(date: string): Promise<TimeSlot[]> {
    const response = await api.get<TimeSlot[]>("/timeslots", {
      params: { date },
    });
    return response.data;
  },

 
  async getTimeSlotById(id: number): Promise<TimeSlot> {
    const response = await api.get<TimeSlot>(`/timeslots/${id}`);
    return response.data;
  },
};