import React from "react";
import type { LaundryService } from "../../types/LaundryService";
import { colors, radius, type, shadow } from "../../styles/Theme";
import { formatCurrency } from "../../utilis/FormatCurrency";

interface ServiceCardProps {
  service: LaundryService;
  onViewDetails: (service: LaundryService) => void;
  onSelect?: (service: LaundryService) => void;
}

const ServiceCard: React.FC<ServiceCardProps> = ({ service, onViewDetails, onSelect }) => {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "8px",
        padding: "18px",
        backgroundColor: colors.surface,
        border: `1px solid ${colors.border}`,
        borderRadius: radius.lg,
        boxShadow: shadow.card,
        height: "100%",
        boxSizing: "border-box",
      }}
    >
      <span
        style={{
          alignSelf: "flex-start",
          padding: "3px 10px",
          borderRadius: radius.pill,
          fontFamily: type.body,
          fontSize: "11px",
          fontWeight: 600,
          backgroundColor: service.isAvailable ? colors.primaryLight : colors.unavailableBg,
          color: service.isAvailable ? colors.primary : colors.unavailableText,
        }}
      >
        {service.isAvailable ? "Available" : "Unavailable"}
      </span>

      <h3 style={{ fontFamily: type.display, fontSize: "17px", margin: 0, color: colors.text }}>
        {service.name}
      </h3>

      <p
        style={{
          fontFamily: type.body,
          fontSize: "13px",
          color: colors.textMuted,
          margin: 0,
          flexGrow: 1,
          display: "-webkit-box",
          WebkitLineClamp: 3,
          WebkitBoxOrient: "vertical",
          overflow: "hidden",
        }}
      >
        {service.description}
      </p>

      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginTop: "6px",
        }}
      >
        <span style={{ fontFamily: type.display, fontSize: "16px", fontWeight: 700, color: colors.text }}>
          {formatCurrency(service.price)}
        </span>

        <div style={{ display: "flex", gap: "8px" }}>
          <button
            type="button"
            onClick={() => onViewDetails(service)}
            style={{
              padding: "7px 14px",
              borderRadius: radius.pill,
              border: `1px solid ${colors.border}`,
              backgroundColor: "transparent",
              color: colors.text,
              fontFamily: type.body,
              fontSize: "12px",
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            Details
          </button>

          {onSelect && (
            <button
              type="button"
              disabled={!service.isAvailable}
              onClick={() => onSelect(service)}
              style={{
                padding: "7px 14px",
                borderRadius: radius.pill,
                border: "none",
                backgroundColor: service.isAvailable ? colors.primary : colors.unavailableBg,
                color: service.isAvailable ? colors.surface : colors.unavailableText,
                fontFamily: type.body,
                fontSize: "12px",
                fontWeight: 600,
                cursor: service.isAvailable ? "pointer" : "not-allowed",
              }}
            >
              Select
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ServiceCard;