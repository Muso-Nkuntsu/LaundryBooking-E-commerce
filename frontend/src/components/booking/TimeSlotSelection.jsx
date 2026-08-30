import { useMemo, useState } from 'react';
import { useTimeSlots } from '../../hooks/useTimeSlots';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorState from '../common/ErrorState';
import styles from './TimeSlotSelection.module.css';

const DAY_LABEL = new Intl.DateTimeFormat(undefined, { weekday: 'short' });
const DAY_NUMBER = new Intl.DateTimeFormat(undefined, { day: 'numeric' });
const MONTH_LABEL = new Intl.DateTimeFormat(undefined, { month: 'short' });

function formatTimeRange(startTime, endTime) {
  return `${startTime} – ${endTime}`;
}

/**
 
 * @param {{
 *   residenceId: string,
 *   onSlotSelected: (slot: import('../../api/timeSlotService').TimeSlot) => void
 * }} props
 */
export default function TimeSlotSelection({ residenceId, onSlotSelected }) {
  const { dates, selectedDate, setSelectedDate, slots, datesStatus, slotsStatus, retry } =
    useTimeSlots(residenceId);
  const [pendingSlotId, setPendingSlotId] = useState(null);

  const pendingSlot = useMemo(
    () => slots.find((s) => s.id === pendingSlotId) ?? null,
    [slots, pendingSlotId]
  );

  function handleDateChange(date) {
    setPendingSlotId(null);
    setSelectedDate(date);
  }

  function handleSlotClick(slot) {
    if (slot.status !== 'available') return;
    setPendingSlotId((current) => (current === slot.id ? null : slot.id));
  }

  function handleConfirm() {
    if (pendingSlot) onSlotSelected(pendingSlot);
  }

  if (datesStatus === 'loading' && dates.length === 0) {
    return <LoadingSpinner label="Loading available dates…" />;
  }

  if (datesStatus === 'error') {
    return <ErrorState message="We couldn't load available dates." onRetry={retry} />;
  }

  if (dates.length === 0) {
    return (
      <div className={styles.empty}>
        <p>There are no upcoming dates open for booking right now. Check back soon.</p>
      </div>
    );
  }

  return (
    <section className={styles.wrap} aria-labelledby="timeslot-heading">
      <header className={styles.header}>
        <h2 id="timeslot-heading" className={styles.heading}>
          Choose a time slot
        </h2>
        <p className={styles.subheading}>Pick a date, then an open slot for the laundry room.</p>
      </header>

      <div className={styles.dateRow} role="tablist" aria-label="Select a date">
        {dates.map(({ date, hasAvailability }) => {
          const d = new Date(`${date}T00:00:00`);
          const isSelected = date === selectedDate;
          return (
            <button
              key={date}
              type="button"
              role="tab"
              aria-selected={isSelected}
              className={`${styles.dateChip} ${isSelected ? styles.dateChipSelected : ''} ${
                !hasAvailability ? styles.dateChipDim : ''
              }`}
              onClick={() => handleDateChange(date)}
            >
              <span className={styles.dateChipDay}>{DAY_LABEL.format(d)}</span>
              <span className={styles.dateChipNumber}>{DAY_NUMBER.format(d)}</span>
              <span className={styles.dateChipMonth}>{MONTH_LABEL.format(d)}</span>
              {!hasAvailability && <span className={styles.dateChipFullTag}>Full</span>}
            </button>
          );
        })}
      </div>

      <div className={styles.legend}>
        <span className={styles.legendItem}>
          <span className={`${styles.legendSwatch} ${styles.legendAvailable}`} /> Available
        </span>
        <span className={styles.legendItem}>
          <span className={`${styles.legendSwatch} ${styles.legendSelected}`} /> Selected
        </span>
        <span className={styles.legendItem}>
          <span className={`${styles.legendSwatch} ${styles.legendUnavailable}`} /> Booked
        </span>
      </div>

      {slotsStatus === 'loading' && <LoadingSpinner label="Loading time slots…" />}

      {slotsStatus === 'error' && (
        <ErrorState message="We couldn't load slots for this date." onRetry={retry} />
      )}

      {slotsStatus === 'success' && slots.length === 0 && (
        <div className={styles.empty}>
          <p>No slots are configured for this date yet. Try another date.</p>
        </div>
      )}

      {slotsStatus === 'success' && slots.length > 0 && (
        <ul className={styles.slotGrid}>
          {slots.map((slot) => {
            const isAvailable = slot.status === 'available';
            const isBooked = slot.status === 'booked';
            const isSelected = slot.id === pendingSlotId;

            return (
              <li key={slot.id}>
                <button
                  type="button"
                  className={`${styles.ticket} ${isSelected ? styles.ticketSelected : ''} ${
                    !isAvailable ? styles.ticketUnavailable : ''
                  }`}
                  disabled={!isAvailable}
                  aria-pressed={isSelected}
                  aria-label={
                    isAvailable
                      ? `Select ${formatTimeRange(slot.startTime, slot.endTime)}`
                      : `${formatTimeRange(slot.startTime, slot.endTime)}, ${
                          isBooked ? 'already booked' : 'unavailable'
                        }`
                  }
                  onClick={() => handleSlotClick(slot)}
                >
                  <span className={styles.ticketStub}>
                    {slot.machineType === 'dryer' ? '🌀' : '🫧'}
                  </span>
                  <span className={styles.ticketBody}>
                    <span className={styles.ticketTime}>
                      {formatTimeRange(slot.startTime, slot.endTime)}
                    </span>
                    <span className={styles.ticketStatus}>
                      {isSelected ? 'Selected' : isAvailable ? 'Available' : 'Booked'}
                    </span>
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      )}

      <div className={styles.footer}>
        <p className={styles.footerHint}>
          {pendingSlot
            ? `${formatTimeRange(pendingSlot.startTime, pendingSlot.endTime)} on ${selectedDate}`
            : 'Select an available slot to continue.'}
        </p>
        <button
          type="button"
          className={styles.confirmButton}
          disabled={!pendingSlot}
          onClick={handleConfirm}
        >
          Continue to booking
        </button>
      </div>
    </section>
  );
}