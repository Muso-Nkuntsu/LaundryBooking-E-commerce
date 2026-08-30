import { Navigate, Link } from 'react-router-dom';
import { useBooking } from '../../context/useBooking';
import styles from './BookingConfirmationPage.module.css';

function formatPrice(price, currency = 'ZAR') {
  return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(price);
}

export default function BookingConfirmationPage() {
  const { selectedSlot, selectedServices, clearBooking } = useBooking();

  if (!selectedSlot) {
    
    return <Navigate to="/booking" replace />;
  }

  const total = selectedServices.reduce((sum, s) => sum + s.price, 0);

  return (
    <section className={styles.wrap} aria-labelledby="confirm-heading">
      <h2 id="confirm-heading" className={styles.heading}>
        Confirm your booking
      </h2>

      <div className={styles.summaryCard}>
        <h3 className={styles.summaryLabel}>Time slot</h3>
        <p className={styles.slotLine}>
          {selectedSlot.date} · {selectedSlot.startTime} – {selectedSlot.endTime}
        </p>

        {selectedServices.length > 0 && (
          <>
            <h3 className={styles.summaryLabel}>Services</h3>
            <ul className={styles.serviceList}>
              {selectedServices.map((s) => (
                <li key={s.id} className={styles.serviceRow}>
                  <span>{s.name}</span>
                  <span>{formatPrice(s.price, s.currency)}</span>
                </li>
              ))}
            </ul>
            <p className={styles.total}>Total: {formatPrice(total)}</p>
          </>
        )}
      </div>

      <div className={styles.actions}>
        <Link to="/laundry" className={styles.secondaryAction}>
          Add another service
        </Link>
        <button type="button" className={styles.primaryAction} onClick={clearBooking}>
          Confirm booking
        </button>
      </div>
    </section>
  );
}