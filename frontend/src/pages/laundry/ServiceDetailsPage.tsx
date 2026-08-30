import { useNavigate, useParams } from 'react-router-dom';
import ServiceDetails from '../components/laundry/ServiceDetails';
import { useBooking } from '../context/useBooking';


export default function ServiceDetailsPage() {
  const { serviceId } = useParams();
  const { selectedServices, toggleService } = useBooking();
  const navigate = useNavigate();

  const isSelected = selectedServices.some((s) => s.id === serviceId);

  return (
    <ServiceDetails
      serviceId={serviceId}
      isSelected={isSelected}
      onSelect={toggleService}
      onBack={() => navigate('/laundry')}
    />
  );
}