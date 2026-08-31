import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { LaundryService } from "../../types/LaundryService";
import { laundryServiceService } from "../../services/LaundryServiceService";
import { getApiErrorMessage } from "../../services/Api";
import { colors, radius, type, shadow } from "../../styles/Theme";
import { formatCurrency } from "../../utilis/FormatCurrency";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";

const ServiceDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [service, setService] = useState<LaundryService | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadService = async () => {
    if (!id) {
      setError("No service was specified.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const data = await laundryServiceService.getServiceById(Number(id));
      setService(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadService();
    
  }, [id]);

  const handleAddToBooking = () => {
    if (!service) return;
    navigate("/bookings/create", {
      state: { laundryServiceId: service.id, laundryServiceName: service.name },
    });
  };

  return (
    <div
      style={{
        maxWidth: "640px",
        margin: "0 auto",
        padding: "clamp(16px, 4vw, 32px)",
        boxSizing: "border-box",
      }}
    >
      <button
        type="button"
        onClick={() => navigate(-1)}
        style={{
          border: "none",
          background: "none",
          color: colors.textMuted,
          fontFamily: type.body,
          fontSize: "13px",
          cursor: "pointer",
          padding: 0,
          marginBottom: "16px",
        }}
      >
        &larr; Back to services
      </button>

      {loading && <Loading message="Loading service details..." />}

      {!loading && error && (
        <ErrorMessage message={error} onRetry={loadService} />
      )}

      {!loading && !error && service && (
        <div
          style={{
            backgroundColor: colors.surface,
            border: `1px solid ${colors.border}`,
            borderRadius: radius.lg,
            boxShadow: shadow.card,
            padding: "clamp(18px, 4vw, 28px)",
          }}
        >
          <span
            style={{
              display: "inline-block",
              padding: "3px 10px",
              borderRadius: radius.pill,
              fontFamily: type.body,
              fontSize: "11px",
              fontWeight: 600,
              backgroundColor: service.isAvailable
                ? colors.primaryLight
                : colors.unavailableBg,
              color: service.isAvailable ? colors.primary : colors.unavailableText,
              marginBottom: "12px",
            }}
          >
            {service.isAvailable ? "Available" : "Unavailable"}
          </span>

          <h1
            style={{
              fontFamily: type.display,
              fontSize: "clamp(22px, 4vw, 28px)",
              color: colors.text,
              margin: "0 0 8px",
            }}
          >
            {service.name}
          </h1>

          <p
            style={{
              fontFamily: type.body,
              fontSize: "15px",
              lineHeight: 1.6,
              color: colors.textMuted,
              margin: "0 0 20px",
              maxWidth: "60ch",
            }}
          >
            {service.description}
          </p>

          <div
            style={{
              display: "flex",
              gap: "24px",
              flexWrap: "wrap",
              padding: "16px 0",
              borderTop: `1px solid ${colors.border}`,
              borderBottom: `1px solid ${colors.border}`,
              marginBottom: "24px",
            }}
          >
            <div>
              <div style={{ fontFamily: type.body, fontSize: "12px", color: colors.textMuted }}>
                Price
              </div>
              <div
                style={{
                  fontFamily: type.display,
                  fontSize: "20px",
                  fontWeight: 700,
                  color: colors.text,
                }}
              >
                {formatCurrency(service.price)}
              </div>
            </div>

            {service.durationMinutes !== undefined && (
              <div>
                <div style={{ fontFamily: type.body, fontSize: "12px", color: colors.textMuted }}>
                  Duration
                </div>
                <div
                  style={{
                    fontFamily: type.display,
                    fontSize: "20px",
                    fontWeight: 700,
                    color: colors.text,
                  }}
                >
                  {service.durationMinutes} min
                </div>
              </div>
            )}
          </div>

          <button
            type="button"
            disabled={!service.isAvailable}
            onClick={handleAddToBooking}
            style={{
              width: "100%",
              padding: "13px",
              borderRadius: radius.pill,
              border: "none",
              backgroundColor: service.isAvailable ? colors.primary : colors.unavailableBg,
              color: service.isAvailable ? colors.surface : colors.unavailableText,
              fontFamily: type.body,
              fontSize: "14px",
              fontWeight: 700,
              cursor: service.isAvailable ? "pointer" : "not-allowed",
            }}
          >
            {service.isAvailable ? "Add to booking" : "Currently unavailable"}
          </button>
        </div>
      )}
    </div>
  );
};

export default ServiceDetails;