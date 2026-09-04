export type TimeSlotStatus = "AVAILABLE" | "BOOKED" | "UNAVAILABLE";

export interface TimeSlot {
  id: number;
  date: string; 
  startTime: string; 
  endTime: string; 
  status: TimeSlotStatus;
  machineId?: number;
  machineName?: string;
}


export interface DateGroup {
  date: string;
  slots: TimeSlot[];
  hasAvailability: boolean;
}