export type BookingStatus =
    | "CONFIRMED"
    | "CANCELLED"
    | "COMPLETED"
    | "PENDING" ;


export interface LaundryRoom{
    roomId : number;
    roomNumber: string;
    location: string;
    capacity: number;
    description: string;
    active?: boolean;
    isActive?: boolean;
}

export interface LaundryMachine{
    machineId: number;
    machineNumber: string;
    type: string;
    status: string;
    laundryRoom: LaundryRoom;
}

export interface TimeSlot {
  id: number;
  startTime: string;
  endTime: string;
  date: string;
  available?: boolean;
  isAvailable?: boolean;
}

export interface Booking {
  id: number;
  bookingDate: string;
  status: BookingStatus;
  totalAmount: number;
  student?: {
    studentId: number;
  };
  laundryMachine: LaundryMachine;
  timeSlot: TimeSlot;
}

export interface CreateBookingRequest {
  studentId: number;
  machineId: number;
  timeSlotId: number;
  serviceId?: number;
  totalAmount: number;
}