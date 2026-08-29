import { useNavigate } from 'react-router-dom';
import LaundryServices from '../components/laundry/LaundryServices';
import { useBooking } from '../context/useBooking';

export default function LaundryPage() {
  const { residenceId, selectedServices, toggleService } = useBooking();
  const navigate = useNavigate();

  return (
    <LaundryServices
      residenceId={residenceId}
      selectedServiceIds={selectedServices.map((s) => s.id)}
      onSelectService={toggleService}
      onViewDetails={(service) => navigate(`/laundry/${service.id}`)}
    />
  );
}