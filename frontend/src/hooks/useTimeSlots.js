import { useCallback, useEffect, useState } from 'react';
import { getAvailableDates, getTimeSlots } from '../api/timeSlotService';

/**
 * Drives the Time Slot Selection screen: loads which dates have availability,
 * then loads slots for whichever date is selected (defaulting to the first
 * available date).
 *
 * @param {string} residenceId
 */
export function useTimeSlots(residenceId) {
  const [dates, setDates] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [slots, setSlots] = useState([]);

  const [datesStatus, setDatesStatus] = useState('idle'); // idle | loading | success | error
  const [slotsStatus, setSlotsStatus] = useState('idle');
  const [error, setError] = useState(null);

  const loadDates = useCallback(() => {
    if (!residenceId) return;
    const controller = new AbortController();
    setDatesStatus('loading');
    setError(null);

    getAvailableDates(residenceId, { signal: controller.signal })
      .then((result) => {
        setDates(result);
        setDatesStatus('success');
        const firstAvailable = result.find((d) => d.hasAvailability) ?? result[0];
        if (firstAvailable) setSelectedDate(firstAvailable.date);
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setError(err);
        setDatesStatus('error');
      });

    return () => controller.abort();
  }, [residenceId]);

  const loadSlots = useCallback(
    (date) => {
      if (!residenceId || !date) return;
      const controller = new AbortController();
      setSlotsStatus('loading');
      setError(null);

      getTimeSlots(residenceId, date, { signal: controller.signal })
        .then((result) => {
          setSlots(result);
          setSlotsStatus('success');
        })
        .catch((err) => {
          if (err.name === 'AbortError') return;
          setError(err);
          setSlotsStatus('error');
        });

      return () => controller.abort();
    },
    [residenceId]
  );

  useEffect(() => {
    const cleanup = loadDates();
    return cleanup;
  }, [loadDates]);

  useEffect(() => {
    if (!selectedDate) return;
    const cleanup = loadSlots(selectedDate);
    return cleanup;
  }, [selectedDate, loadSlots]);

  const retry = useCallback(() => {
    if (datesStatus === 'error') {
      loadDates();
    } else if (slotsStatus === 'error') {
      loadSlots(selectedDate);
    }
  }, [datesStatus, slotsStatus, selectedDate, loadDates, loadSlots]);

  return {
    dates,
    selectedDate,
    setSelectedDate,
    slots,
    datesStatus,
    slotsStatus,
    error,
    retry,
  };
}