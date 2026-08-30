import type { LaundryRoom } from "../types/LaundryRoom";

const API_URL = "http://localhost:8080/laundry-room";

async function request<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Request failed (${response.status})`);
  }
  return response.json();
}

export const getActiveLaundryRooms = (): Promise<LaundryRoom[]> =>
  request<LaundryRoom[]>(`${API_URL}/active`);

export const getLaundryRoomById = (roomId: number): Promise<LaundryRoom> =>
  request<LaundryRoom>(`${API_URL}/${roomId}`);
