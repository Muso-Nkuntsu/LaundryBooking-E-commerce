import React, { useEffect, useMemo, useState } from "react";
import type { TimeSlot, DateGroup } from "../../types/TimeSlot";
import { timeSlotService } from "../../services/TimeSlotService";
import { getApiErrorMessage } from "../../services/api";
import { colors, radius, type, shadow } from "../../styles/Theme";
import {
  formatWeekday,
  formatDayNumber,
  formatMonth,
  formatFullDate,
  formatTimeRange,
  isToday,
} from "../../utilis/FormatDate";

interface TimeSlotSelectorProps {
  
  onSlotSelect: (slot: TimeSlot) => void;

  selectedSlotId?: number | null;
 
  daysAhead?: number;
}

const groupSlotsByDate = (slots: TimeSlot[]): DateGroup[] => {
  const groups = new Map<string, TimeSlot[]>();
  slots.forEach((slot) => {
    const existing = groups.get(slot.date) ?? [];
    existing.push(slot);
    groups.set(slot.date, existing);
  });

  return Array.from(groups.entries())
    .sort(([dateA], [dateB]) => dateA.localeCompare(dateB))
    .map(([date, dateSlots]) => ({
      date,
      slots: dateSlots.sort((a, b) => a.startTime.localeCompare(b.startTime)),
      hasAvailability: dateSlots.some((s) => s.status === "AVAILABLE"),
    }));
};

const TimeSlotSelector: React.FC<TimeSlotSelectorProps> = ({
  onSlotSelect,
  selectedSlotId = null,
  daysAhead = 7,
}) => {
  const [slots, setSlots] = useState<TimeSlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [internalSelectedId, setInternalSelectedId] = useState<number | null>(
    selectedSlotId
  );

  const loadSlots = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await timeSlotService.getUpcomingTimeSlots(daysAhead);
      setSlots(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots();
 
  }, [daysAhead]);

  const dateGroups = useMemo(() => groupSlotsByDate(slots), [slots]);

  useEffect(() => {
    if (dateGroups.length === 0) {
      setSelectedDate(null);
      return;
    }
    if (!selectedDate || !dateGroups.some((g) => g.date === selectedDate)) {
      const firstAvailable = dateGroups.find((g) => g.hasAvailability);
      setSelectedDate((firstAvailable ?? dateGroups[0]).date);
    }
  }, [dateGroups, selectedDate]);

  const activeGroup = dateGroups.find((g) => g.date === selectedDate) ?? null;

  const handleSelectSlot = (slot: TimeSlot) => {
    if (slot.status !== "AVAILABLE") return;
    setInternalSelectedId(slot.id);
    onSlotSelect(slot);
  };

  if (loading) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "10px",
          padding: "40px 16px",
          fontFamily: type.body,
          color: colors.textMuted,
          fontSize: "14px",
        }}
        role="status"
        aria-live="polite"
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
        Loading available time slots...
      </div>
    );
  }

  if (error) {
    return (
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
        <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.text, margin: 0 }}>
          {error}
        </p>
        <button
          type="button"
          onClick={loadSlots}
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
    );
  }

  if (dateGroups.length === 0) {
    return (
      <p style={{ fontFamily: type.body, fontSize: "14px", color: colors.textMuted }}>
        No time slots are available right now. Please check back later.
      </p>
    );
  }

  return (
    <div style={{ width: "100%" }}>
      {}
      <div
        style={{
          display: "flex",
          gap: "10px",
          overflowX: "auto",
          paddingBottom: "10px",
          marginBottom: "16px",
        }}
      >
        {dateGroups.map((group) => {
          const isSelected = group.date === selectedDate;
          return (
            <button
              key={group.date}
              type="button"
              onClick={() => setSelectedDate(group.date)}
              aria-pressed={isSelected}
              aria-label={`${formatFullDate(group.date)}, ${
                group.hasAvailability ? "slots available" : "fully booked"
              }`}
              style={{
                flex: "0 0 auto",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                minWidth: "64px",
                padding: "10px 8px",
                borderRadius: radius.md,
                border: `1px solid ${isSelected ? colors.primary : colors.border}`,
                backgroundColor: isSelected ? colors.primary : colors.surface,
                cursor: "pointer",
                boxShadow: isSelected ? shadow.card : "none",
                fontFamily: type.body,
              }}
            >
              <span
                style={{
                  fontSize: "11px",
                  fontWeight: 600,
                  letterSpacing: "0.02em",
                  color: isSelected ? colors.surface : colors.textMuted,
                }}
              >
                {isToday(group.date) ? "Today" : formatWeekday(group.date)}
              </span>
              <span
                style={{
                  fontSize: "18px",
                  fontWeight: 700,
                  color: isSelected ? colors.surface : colors.text,
                  fontFamily: type.display,
                }}
              >
                {formatDayNumber(group.date)}
              </span>
              <span
                style={{
                  fontSize: "11px",
                  color: isSelected ? colors.surface : colors.textMuted,
                }}
              >
                {formatMonth(group.date)}
              </span>
              {!group.hasAvailability && (
                <span
                  style={{
                    marginTop: "4px",
                    fontSize: "9px",
                    fontWeight: 600,
                    color: isSelected ? colors.surface : colors.unavailableText,
                  }}
                >
                  FULL
                </span>
              )}
            </button>
          );
        })}
      </div>

      {}
      <div style={{ display: "flex", gap: "16px", marginBottom: "12px", flexWrap: "wrap" }}>
        <LegendItem color={colors.surface} border={colors.primary} label="Available" />
        <LegendItem color={colors.primary} border={colors.primary} label="Selected" />
        <LegendItem color={colors.unavailableBg} border={colors.unavailableBorder} label="Booked" />
      </div>

      {}
      {activeGroup && (
        <p style={{ fontFamily: type.body, fontSize: "13px", color: colors.textMuted, margin: "0 0 10px" }}>
          {formatFullDate(activeGroup.date)}
        </p>
      )}

      {}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(128px, 1fr))",
          gap: "10px",
        }}
      >
        {activeGroup?.slots.map((slot) => {
          const isAvailable = slot.status === "AVAILABLE";
          const isSelected = slot.id === internalSelectedId && isAvailable;

          let bg: string = colors.surface;
          let border: string = colors.border;
          let textColor: string = colors.text;

          if (isSelected) {
            bg = colors.primary;
            border = colors.primary;
            textColor = colors.surface;
          } else if (isAvailable) {
            bg = colors.surface;
            border = colors.primary;
            textColor = colors.primary;
          } else {
            bg = colors.unavailableBg;
            border = colors.unavailableBorder;
            textColor = colors.unavailableText;
          }

          return (
            <button
              key={slot.id}
              type="button"
              disabled={!isAvailable}
              onClick={() => handleSelectSlot(slot)}
              aria-pressed={isSelected}
              aria-disabled={!isAvailable}
              title={isAvailable ? "Available — select this slot" : "This slot is already booked"}
              style={{
                padding: "12px 8px",
                borderRadius: radius.md,
                border: `1px solid ${border}`,
                backgroundColor: bg,
                color: textColor,
                cursor: isAvailable ? "pointer" : "not-allowed",
                fontFamily: type.body,
                fontSize: "13px",
                fontWeight: 600,
                textAlign: "center",
                lineHeight: 1.3,
              }}
            >
              {formatTimeRange(slot.startTime, slot.endTime)}
              {!isAvailable && (
                <div
                  style={{
                    fontSize: "10px",
                    fontWeight: 500,
                    marginTop: "3px",
                    textTransform: "uppercase",
                    letterSpacing: "0.04em",
                  }}
                >
                  Booked
                </div>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};

const LegendItem: React.FC<{ color: string; border: string; label: string }> = ({
  color,
  border,
  label,
}) => (
  <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
    <span
      style={{
        width: "12px",
        height: "12px",
        borderRadius: "4px",
        backgroundColor: color,
        border: `1px solid ${border}`,
        display: "inline-block",
      }}
    />
    <span style={{ fontFamily: type.body, fontSize: "12px", color: colors.textMuted }}>
      {label}
    </span>
  </div>
);

export default TimeSlotSelector;