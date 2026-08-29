import { Link } from 'react-router-dom';
import styles from './HomePage.module.css';

export default function HomePage() {
  return (
    <section className={styles.wrap}>
      <h1 className={styles.title}>Residence laundry booking</h1>
      <p className={styles.subtitle}>
        Reserve a washer or dryer slot and add any extra laundry services you need.
      </p>
      <div className={styles.actions}>
        <Link className={styles.primaryAction} to="/booking">
          Book a time slot
        </Link>
        <Link className={styles.secondaryAction} to="/laundry">
          Browse laundry services
        </Link>
      </div>
    </section>
  );
}