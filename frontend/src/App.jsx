import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import { BookingProvider } from './context/BookingContext';
import HomePage from './pages/HomePage';
import LaundryPage from './pages/LaundryPage';
import ServiceDetailsPage from './pages/ServiceDetailsPage';
import BookingPage from './pages/BookingPage';
import BookingConfirmationPage from './pages/BookingConfirmationPage';
import styles from './App.module.css';

function App() {
  return (
    <BookingProvider>
      <BrowserRouter>
        <div className={styles.shell}>
          <header className={styles.header}>
            <Link to="/" className={styles.brand}>
              🧺 Residence Laundry
            </Link>
            <nav className={styles.nav} aria-label="Primary">
              <Link to="/booking">Book a slot</Link>
              <Link to="/laundry">Services</Link>
            </nav>
          </header>

          <main className={styles.main}>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/booking" element={<BookingPage />} />
              <Route path="/booking/confirm" element={<BookingConfirmationPage />} />
              <Route path="/laundry" element={<LaundryPage />} />
              <Route path="/laundry/:serviceId" element={<ServiceDetailsPage />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </BookingProvider>
  );
}

export default App;