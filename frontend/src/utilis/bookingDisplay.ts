import type { Booking, BookingStatus } from "../types/booking";
import { formatFullDate, formatTimeRange } from "./FormatDate";

export const STATUS_LABEL: Record<BookingStatus, string> = {
  CONFIRMED: "Confirmed",
  PENDING: "Pending",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
};

export const STATUS_PILL: Record<BookingStatus, string> = {
  CONFIRMED: "pill pill-ok",
  PENDING: "pill pill-warn",
  COMPLETED: "pill pill-info",
  CANCELLED: "pill pill-off",
};

export function bookingDate(booking: Booking): string {
  const date = booking.timeSlot?.date;
  return date ? formatFullDate(date) : "Date not set";
}

export function bookingTime(booking: Booking): string {
  const slot = booking.timeSlot;
  return slot?.startTime && slot?.endTime ? formatTimeRange(slot.startTime, slot.endTime) : "Time not set";
}

export function bookingPlace(booking: Booking): string {
  const machine = booking.laundryMachine;
  const room = machine?.laundryRoom;
  const parts = [machine ? `Machine ${machine.machineNumber}` : null, room ? room.roomNumber : null].filter(Boolean);
  return parts.length > 0 ? parts.join(", ") : "Machine not set";
}

export function isUpcoming(booking: Booking): boolean {
  if (booking.status !== "CONFIRMED" && booking.status !== "PENDING") return false;
  const date = booking.timeSlot?.date;
  if (!date) return true;
  const today = new Date().toISOString().slice(0, 10);
  return date >= today;
}

export function byDateAscending(a: Booking, b: Booking): number {
  const keyA = `${a.timeSlot?.date ?? ""} ${a.timeSlot?.startTime ?? ""}`;
  const keyB = `${b.timeSlot?.date ?? ""} ${b.timeSlot?.startTime ?? ""}`;
  return keyA.localeCompare(keyB);
}
