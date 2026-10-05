import React, { useState } from "react";
import { Rating } from "./Rating";
import type { LaundryService } from "../../types/LaundryService";
import { friendlyError } from "../../utilis/errorMessage";

export interface ReviewFormValues {
  serviceId: number;
  rating: number;
  comment: string;
}

interface ReviewFormProps {
  services: LaundryService[];
  onSubmit: (values: ReviewFormValues) => Promise<void>;
}

const MIN_LENGTH = 10;

export const ReviewForm: React.FC<ReviewFormProps> = ({ services, onSubmit }) => {
  const [serviceId, setServiceId] = useState<string>("");
  const [rating, setRating] = useState<number>(0);
  const [comment, setComment] = useState<string>("");
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const errors: { serviceId?: string; rating?: string; comment?: string } = {};
  if (!serviceId) errors.serviceId = "Choose the service you are reviewing.";
  if (rating === 0) errors.rating = "Choose a star rating.";
  if (!comment.trim()) errors.comment = "Write a few words about your experience.";
  else if (comment.trim().length < MIN_LENGTH) errors.comment = `Write at least ${MIN_LENGTH} characters.`;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    setServerError(null);

    if (Object.keys(errors).length > 0) return;

    try {
      setIsSubmitting(true);
      await onSubmit({ serviceId: Number(serviceId), rating, comment: comment.trim() });
      setServiceId("");
      setRating(0);
      setComment("");
      setSubmitted(false);
    } catch (err) {
      setServerError(friendlyError(err, "We couldn't post your review. Try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (services.length === 0) {
    return (
      <div className="card">
        <h2>Leave a review</h2>
        <p className="muted" style={{ marginTop: 8 }}>Reviews open once laundry services have been added.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="card stack" noValidate>
      <h2>Leave a review</h2>

      {serverError && <div className="alert alert-error" role="alert">{serverError}</div>}

      <div className="field">
        <label htmlFor="review-service">Service</label>
        <select
          id="review-service"
          className="input"
          value={serviceId}
          onChange={(e) => setServiceId(e.target.value)}
          aria-invalid={submitted && errors.serviceId ? true : undefined}
        >
          <option value="">Choose a service</option>
          {services.map((service) => (
            <option key={service.id} value={service.id}>{service.name}</option>
          ))}
        </select>
        {submitted && errors.serviceId && <span className="field-error">{errors.serviceId}</span>}
      </div>

      <div className="field">
        <span style={{ fontWeight: 600, fontSize: "0.925rem" }}>Your rating</span>
        <Rating value={rating} onChange={setRating} size="lg" />
        {submitted && errors.rating && <span className="field-error">{errors.rating}</span>}
      </div>

      <div className="field">
        <label htmlFor="comment">Your feedback</label>
        <textarea
          id="comment"
          className="input"
          rows={3}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="What went well, and what could be better?"
          aria-invalid={submitted && errors.comment ? true : undefined}
        />
        {submitted && errors.comment ? (
          <span className="field-error">{errors.comment}</span>
        ) : (
          <span className="field-hint">{comment.trim().length} characters</span>
        )}
      </div>

      <div>
        <button type="submit" disabled={isSubmitting} className="btn btn-primary">
          {isSubmitting ? "Posting..." : "Post review"}
        </button>
      </div>
    </form>
  );
};
