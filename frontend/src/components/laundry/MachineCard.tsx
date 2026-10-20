import React from "react";
import type { LaundryMachine } from "../../types/LaundryMachine";
import { colors, radius, type, shadow } from "../../styles/Theme";

interface MachineCardProps {
    machine: LaundryMachine;
    onViewDetails: (machine: LaundryMachine) => void;
}

const MachineCard: React.FC<MachineCardProps> = ({ machine, onViewDetails }) => {
    const isAvailable = machine.status === "AVAILABLE";

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
              backgroundColor: isAvailable ? colors.primaryLight : colors.unavailableBg,
              color: isAvailable ? colors.primary : colors.unavailableText,
          }}
      >
        {machine.status}
      </span>

            <h3 style={{ fontFamily: type.display, fontSize: "17px", margin: 0, color: colors.text }}>
                Machine {machine.machineNumber}
            </h3>

            <p style={{ fontFamily: type.body, fontSize: "13px", color: colors.textMuted, margin: 0 }}>
                Type: {machine.type}
            </p>

            <div style={{ marginTop: "6px" }}>
                <button
                    type="button"
                    onClick={() => onViewDetails(machine)}
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
                    View Details
                </button>
            </div>
        </div>
    );
};

export default MachineCard;