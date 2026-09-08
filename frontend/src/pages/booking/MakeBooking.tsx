import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import type { TimeSlot } from "../../types/TimeSlot";
import { colors, radius, type } from "../../styles/Theme";
import { formatFullDate, formatTimeRange } from "../../utilis/FormatDate";
import TimeSlotSelector from "../../components/booking/TimeSlotSelector";

interface MakeBookingLocationState {
  laundryServiceId?: number;
  laundryServiceName?: string;
}

const MakeBooking: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { laundryServiceId, laundryServiceName } = (location.state as MakeBookingLocationState) ?? {};

  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);

  const handleConfirm = () => {
    if (!selectedSlot) return;
    navigate("/bookings/confirmation", {
      state: { timeSlot: selectedSlot, laundryServiceId, laundryServiceName },
    });
  };

  return (
    <div style={{ maxWidth: "720px", margin: "0 auto", padding: "clamp(16px, 4vw, 32px)", boxSizing: "border-box" }}>
      <header style={{ marginBottom: "20px" }}>
        <h1 style={{ fontFamily: type.display, fontSize: "clamp(22px, 4vw, 28px)", color: colors.text, margin: "0 0 4px" }}>
          Choose a time slot
        </h1>
        <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.textMuted, margin: 0 }}>
          Pick a date and an available slot for your laundry booking.
        </p>
      </header>

      {laundryServiceName && (
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "12px 16px",
            backgroundColor: colors.accentLight,
            borderRadius: radius.md,
            marginBottom: "20px",
          }}
        >
          <span style={{ fontFamily: type.body, fontSize: "13px", color: colors.text }}>
            Added service: <strong>{laundryServiceName}</strong>
          </span>
        </div>
      )}

      <TimeSlotSelector onSlotSelect={setSelectedSlot} selectedSlotId={selectedSlot?.id ?? null} />

      <div
        style={{
          position: "sticky",
          bottom: 0,
          marginTop: "24px",
          paddingTop: "16px",
          borderTop: `1px solid ${colors.border}`,
          backgroundColor: colors.bg,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: "12px",
        }}
      >
        <span style={{ fontFamily: type.body, fontSize: "13px", color: colors.textMuted }}>
          {selectedSlot
            ? `Selected: ${formatFullDate(selectedSlot.date)}, ${formatTimeRange(
                selectedSlot.startTime,
                selectedSlot.endTime
              )}`
            : "No time slot selected yet"}
        </span>

        <button
          type="button"
          disabled={!selectedSlot}
          onClick={handleConfirm}
          style={{
            padding: "12px 26px",
            borderRadius: radius.pill,
            border: "none",
            backgroundColor: selectedSlot ? colors.primary : colors.unavailableBg,
            color: selectedSlot ? colors.surface : colors.unavailableText,
            fontFamily: type.body,
            fontSize: "14px",
            fontWeight: 700,
            cursor: selectedSlot ? "pointer" : "not-allowed",
          }}
        >
          Continue to confirmation
        </button>
      </div>
    </div>
  );
};

export default MakeBooking;