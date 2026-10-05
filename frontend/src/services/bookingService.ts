import { apiGet, apiPost, apiPut } from "./Api";
import type {
  Booking,
  CreateBookingRequest,
  LaundryMachine,
  LaundryRoom,
  TimeSlot,
} from "../types/booking";

export const bookingService = {
  // ---------------- LAUNDRY ROOMS ----------------

  getActiveRooms(): Promise<LaundryRoom[]> {
    return apiGet<LaundryRoom[]>("/laundry-room/active");
  },

  // ---------------- MACHINES ----------------

  getAllMachines(): Promise<LaundryMachine[]> {
    return apiGet<LaundryMachine[]>("/laundrymachine/getall");
  },

  /** Machines that are not out of order and not already booked for this time slot. */
  getAvailableMachines(timeSlotId: number): Promise<LaundryMachine[]> {
    return apiGet<LaundryMachine[]>("/laundrymachine/available", { timeSlotId });
  },

  // ---------------- TIME SLOTS ----------------

  getAllTimeSlots(): Promise<TimeSlot[]> {
    return apiGet<TimeSlot[]>("/timeslot/all");
  },

  // ---------------- BOOKINGS ----------------

  /** The backend works out the total from the chosen service. */
  createBooking(booking: CreateBookingRequest): Promise<Booking> {
    return apiPost<Booking>("/api/bookings", undefined, {
      studentId: booking.studentId,
      machineId: booking.machineId,
      timeSlotId: booking.timeSlotId,
      serviceId: booking.serviceId,
    });
  },

  getBookingsByStudent(studentId: number): Promise<Booking[]> {
    return apiGet<Booking[]>(`/api/bookings/student/${studentId}`);
  },

  cancelBooking(bookingId: number): Promise<Booking> {
    return apiPut<Booking>(`/api/bookings/${bookingId}/cancel`);
  },
};
