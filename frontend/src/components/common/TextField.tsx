import { useState } from "react";
import type { InputHTMLAttributes } from "react";

interface TextFieldProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  hint?: string;
  className?: string;
}

function TextField({ id, label, value, onChange, error, hint, type = "text", className, ...rest }: TextFieldProps) {
  const [reveal, setReveal] = useState(false);
  const isPassword = type === "password";
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;

  const input = (
    <input
      {...rest}
      id={id}
      className="input"
      type={isPassword && reveal ? "text" : type}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      aria-invalid={error ? true : undefined}
      aria-describedby={describedBy}
    />
  );

  return (
    <div className={`field ${className ?? ""}`}>
      <label htmlFor={id}>{label}</label>
      {isPassword ? (
        <div className="input-wrap">
          {input}
          <button type="button" className="link-btn small toggle" onClick={() => setReveal((v) => !v)} aria-pressed={reveal}>
            {reveal ? "Hide" : "Show"}
          </button>
        </div>
      ) : (
        input
      )}
      {error ? (
        <span id={`${id}-error`} className="field-error">{error}</span>
      ) : hint ? (
        <span id={`${id}-hint`} className="field-hint">{hint}</span>
      ) : null}
    </div>
  );
}

export default TextField;
