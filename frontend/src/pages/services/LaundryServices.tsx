import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { LaundryService } from "../../types/LaundryService";
import { laundryServiceService } from "../../services/laundryServiceService";
import { getApiErrorMessage } from "../../services/api";
import { colors, type } from "../../styles/theme";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import ServiceCard from "../../components/services/ServiceCard";

const LaundryServices: React.FC = () => {
  const navigate = useNavigate();
  const [services, setServices] = useState<LaundryService[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadServices = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await laundryServiceService.getAllServices();
      setServices(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadServices();
  }, []);

  const goToDetails = (service: LaundryService) =>
    navigate(`/services/${service.id}`);

  const selectService = (service: LaundryService) =>
    navigate("/bookings/create", {
      state: { laundryServiceId: service.id, laundryServiceName: service.name },
    });

  return (
    <div
      style={{
        maxWidth: "960px",
        margin: "0 auto",
        padding: "clamp(16px, 4vw, 32px)",
        boxSizing: "border-box",
      }}
    >
      <header style={{ marginBottom: "20px" }}>
        <h1
          style={{
            fontFamily: type.display,
            fontSize: "clamp(22px, 4vw, 28px)",
            color: colors.text,
            margin: "0 0 4px",
          }}
        >
          Laundry services
        </h1>
        <p
          style={{
            fontFamily: type.body,
            fontSize: "14px",
            color: colors.textMuted,
            margin: 0,
          }}
        >
          Extra services offered alongside your machine booking.
        </p>
      </header>

      {loading && <Loading message="Loading laundry services..." />}

      {!loading && error && (
        <ErrorMessage message={error} onRetry={loadServices} />
      )}

      {!loading && !error && services.length === 0 && (
        <ErrorMessage message="No laundry services are available at the moment." />
      )}

      {!loading && !error && services.length > 0 && (
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))",
            gap: "16px",
          }}
        >
          {services.map((service) => (
            <ServiceCard
              key={service.id}
              service={service}
              onViewDetails={goToDetails}
              onSelect={selectService}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default LaundryServices;