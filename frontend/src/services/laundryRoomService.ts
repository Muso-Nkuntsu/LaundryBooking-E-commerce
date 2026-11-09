import { apiGet } from "./Api";
import type { LaundryRoom } from "../types/LaundryRoom";

// The backend sends the flag as "active"; the app uses "isActive".
type RawRoom = Omit<LaundryRoom, "isActive"> & { active?: boolean; isActive?: boolean };

const toRoom = (raw: RawRoom): LaundryRoom => ({
  ...raw,
  isActive: raw.isActive ?? raw.active ?? false,
});

export const getActiveLaundryRooms = async (): Promise<LaundryRoom[]> =>
  (await apiGet<RawRoom[]>("/laundry-room/active")).map(toRoom);

export const getLaundryRoomById = async (roomId: number): Promise<LaundryRoom> =>
  toRoom(await apiGet<RawRoom>(`/laundry-room/${roomId}`));
