import type {
  Booking,
  CreateBookingRequest,
  LaundryMachine,
  LaundryRoom,
  TimeSlot,
} from "../types/booking";

const API_BASE_URL = "http://localhost:8080";

export const bookingService = {

  // ---------------- LAUNDRY ROOMS ----------------

  async getActiveRooms(): Promise<LaundryRoom[]> {
    const response = await fetch(
      `${API_BASE_URL}/laundry-room/active`
    );

    if (!response.ok) {
      throw new Error("Failed to load laundry rooms");
    }

    return response.json();
  },


  // ---------------- MACHINES ----------------

  async getAllMachines(): Promise<LaundryMachine[]> {
    const response = await fetch(
      `${API_BASE_URL}/laundrymachine/getall`
    );

    if (!response.ok) {
      throw new Error("Failed to load laundry machines");
    }

    return response.json();
  },


  // ---------------- TIME SLOTS ----------------

  async getAllTimeSlots(): Promise<TimeSlot[]> {
    const response = await fetch(
      `${API_BASE_URL}/timeslot/all`
    );

    if (!response.ok) {
      throw new Error("Failed to load time slots");
    }

    return response.json();
  },


  // ---------------- BOOKINGS ----------------

  async createBooking(
    booking: CreateBookingRequest
  ): Promise<Booking> {

    const params = new URLSearchParams({
      studentId: booking.studentId.toString(),
      machineId: booking.machineId.toString(),
      timeSlotId: booking.timeSlotId.toString(),
      totalAmount: booking.totalAmount.toString(),
    });

    if (booking.serviceId) {
      params.append(
        "serviceId",
        booking.serviceId.toString()
      );
    }

    const response = await fetch(
      `${API_BASE_URL}/api/bookings?${params.toString()}`,
      {
        method: "POST",
      }
    );

    if (!response.ok) {
      const errorText = await response.text();

      throw new Error(
        errorText || "Failed to create booking"
      );
    }

    return response.json();
  },


  // ---------------- STUDENT BOOKINGS ----------------

  async getBookingsByStudent(
    studentId: number
  ): Promise<Booking[]> {

    const response = await fetch(
      `${API_BASE_URL}/api/bookings/student/${studentId}`
    );

    if (!response.ok) {
      throw new Error("Failed to load bookings");
    }

    return response.json();
  },


  // ---------------- CANCEL BOOKING ----------------

  async cancelBooking(
    bookingId: number
  ): Promise<Booking> {

    const response = await fetch(
      `${API_BASE_URL}/api/bookings/${bookingId}/cancel`,
      {
        method: "PUT",
      }
    );

    if (!response.ok) {
      throw new Error("Failed to cancel booking");
    }

    return response.json();
  },
};