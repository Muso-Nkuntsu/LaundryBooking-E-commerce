import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { LaundryService } from "../../types/LaundryService";
import { laundryServiceService } from "../../services/LaundryServiceService";
import { getApiErrorMessage } from "../../services/api";
import { colors, radius, type } from "../../styles/Theme";
import ServiceCard from "../../components/laundry/ServiceCard";

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

  const goToDetails = (service: LaundryService) => navigate(`/laundry/${service.id}`);

  const selectService = (service: LaundryService) =>
    navigate("/bookings/create", {
      state: { laundryServiceId: service.id, laundryServiceName: service.name },
    });

  return (
    <div style={{ maxWidth: "960px", margin: "0 auto", padding: "clamp(16px, 4vw, 32px)", boxSizing: "border-box" }}>
      <header style={{ marginBottom: "20px" }}>
        <h1 style={{ fontFamily: type.display, fontSize: "clamp(22px, 4vw, 28px)", color: colors.text, margin: "0 0 4px" }}>
          Laundry services
        </h1>
        <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.textMuted, margin: 0 }}>
          Extra services offered alongside your machine booking.
        </p>
      </header>

      {}
      {loading && (
        <div
          role="status"
          aria-live="polite"
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: "10px",
            padding: "48px 16px",
            fontFamily: type.body,
            color: colors.textMuted,
            fontSize: "14px",
          }}
        >
          <span
            style={{
              width: "28px",
              height: "28px",
              borderRadius: "50%",
              border: `3px solid ${colors.primaryLight}`,
              borderTopColor: colors.primary,
            }}
          />
          Loading laundry services...
        </div>
      )}

      {}
      {!loading && error && (
        <div
          role="alert"
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            textAlign: "center",
            gap: "10px",
            padding: "28px 20px",
            backgroundColor: colors.dangerLight,
            border: `1px solid ${colors.dangerBorder}`,
            borderRadius: radius.md,
          }}
        >
          <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.text, margin: 0 }}>{error}</p>
          <button
            type="button"
            onClick={loadServices}
            style={{
              padding: "8px 18px",
              borderRadius: radius.pill,
              border: `1px solid ${colors.danger}`,
              backgroundColor: "transparent",
              color: colors.danger,
              fontFamily: type.body,
              fontSize: "13px",
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            Try again
          </button>
        </div>
      )}

      {!loading && !error && services.length === 0 && (
        <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.textMuted }}>
          No laundry services are available at the moment.
        </p>
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
            <ServiceCard key={service.id} service={service} onViewDetails={goToDetails} onSelect={selectService} />
          ))}
        </div>
      )}
    </div>
  );
};

export default LaundryServices;