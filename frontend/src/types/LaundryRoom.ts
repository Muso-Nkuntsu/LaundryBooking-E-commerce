export interface LaundryMachineSummary {
  machineId: number;
  machineNumber: string;
  type: string;
  status: string;
}

export interface LaundryRoom {
  roomId: number;
  roomNumber: string;
  location: string;
  capacity: number;
  description?: string;
  isActive: boolean;
  machines?: LaundryMachineSummary[];
}
