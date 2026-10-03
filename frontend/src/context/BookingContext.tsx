import { createContext, useState } from "react";
import type { ReactNode } from "react";

export interface TimeSlot {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
}

export interface LaundryService {
  id: string;
  name: string;
  price: number;
  currency?: string;
}

export interface BookingContextType {
  residenceId: string;
  selectedSlot: TimeSlot | null;
  setSelectedSlot: (slot: TimeSlot | null) => void;
  selectedServices: LaundryService[];
  toggleService: (service: LaundryService) => void;
  clearBooking: () => void;
}

export const BookingContext = createContext<BookingContextType | null>(null);

interface BookingProviderProps {
  children: ReactNode;
}

export function BookingProvider({ children }: BookingProviderProps) {
  const [residenceId] = useState<string>("default-residence");
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [selectedServices, setSelectedServices] = useState<LaundryService[]>(
    []
  );

  function toggleService(service: LaundryService) {
    setSelectedServices((prev) => {
      const exists = prev.some((s) => s.id === service.id);

      if (exists) {
        return prev.filter((s) => s.id !== service.id);
      }

      return [...prev, service];
    });
  }

  function clearBooking() {
    setSelectedSlot(null);
    setSelectedServices([]);
  }

  return (
    <BookingContext.Provider
      value={{
        residenceId,
        selectedSlot,
        setSelectedSlot,
        selectedServices,
        toggleService,
        clearBooking,
      }}
    >
      {children}
    </BookingContext.Provider>
  );
}

