import { createContext, useCallback, useMemo, useState } from 'react';

export const BookingContext = createContext(null);

const DEFAULT_RESIDENCE_ID = 'res-jasmine-hall';

export function BookingProvider({ children }) {
  const [residenceId] = useState(DEFAULT_RESIDENCE_ID);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [selectedServices, setSelectedServices] = useState([]);

  const toggleService = useCallback((service) => {
    setSelectedServices((prev) => {
      const exists = prev.some((s) => s.id === service.id);
      return exists ? prev.filter((s) => s.id !== service.id) : [...prev, service];
    });
  }, []);

  const clearBooking = useCallback(() => {
    setSelectedSlot(null);
    setSelectedServices([]);
  }, []);

  const value = useMemo(
    () => ({
      residenceId,
      selectedSlot,
      setSelectedSlot,
      selectedServices,
      toggleService,
      clearBooking,
    }),
    [residenceId, selectedSlot, selectedServices, toggleService, clearBooking]
  );

  return <BookingContext.Provider value={value}>{children}</BookingContext.Provider>;
}