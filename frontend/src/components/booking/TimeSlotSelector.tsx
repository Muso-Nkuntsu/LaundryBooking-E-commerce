import React, { useEffect, useMemo, useState } from "react";
import { TimeSlot, DateGroup } from "../../types/TimeSlot";
import { timeSlotService } from "../../services/timeSlotService";
import { getApiErrorMessage } from "../../services/api";
import { colors, radius, type, shadow } from "../../styles/theme";
import {
  formatWeekday,
  formatDayNumber,
  formatMonth,
  formatFullDate,
  formatTimeRange,
  isToday,
} from "../../utils/formatDate";
import Loading from "../common/Loading";
import ErrorMessage from "../common/ErrorMessage";

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
    return <Loading message="Loading available time slots..." />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={loadSlots} />;
  }

  if (dateGroups.length === 0) {
    return (
      <ErrorMessage message="No time slots are available right now. Please check back later." />
    );
  }

  return (
    <div style={{ width: "100%" }}>
      {/* Date strip */}
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
                border: `1px solid ${
                  isSelected ? colors.primary : colors.border
                }`,
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
      <div
        style={{
          display: "flex",
          gap: "16px",
          marginBottom: "12px",
          flexWrap: "wrap",
        }}
      >
        <LegendItem color={colors.surface} border={colors.primary} label="Available" />
        <LegendItem color={colors.primary} border={colors.primary} label="Selected" />
        <LegendItem
          color={colors.unavailableBg}
          border={colors.unavailableBorder}
          label="Booked"
        />
      </div>

      {}
      {activeGroup && (
        <p
          style={{
            fontFamily: type.body,
            fontSize: "13px",
            color: colors.textMuted,
            margin: "0 0 10px",
          }}
        >
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
          const isSelected =
            slot.id === internalSelectedId && isAvailable;

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
              title={
                isAvailable
                  ? "Available — select this slot"
                  : "This slot is already booked"
              }
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