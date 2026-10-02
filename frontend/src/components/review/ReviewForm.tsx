import React, { useState } from "react";
import { Rating } from "./Rating";
import type { CreateReviewPayload } from "../../types/review";
import { friendlyError } from "../../utilis/errorMessage";

interface ReviewFormProps {
  onSubmit: (payload: CreateReviewPayload) => Promise<void>;
}

const MIN_LENGTH = 10;

export const ReviewForm: React.FC<ReviewFormProps> = ({ onSubmit }) => {
  const [rating, setRating] = useState<number>(0);
  const [comment, setComment] = useState<string>("");
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const errors: { rating?: string; comment?: string } = {};
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
      await onSubmit({ rating, comment: comment.trim() });
      setRating(0);
      setComment("");
      setSubmitted(false);
    } catch (err) {
      setServerError(friendlyError(err, "We couldn't post your review. Try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="card stack" noValidate>
      <h2>Leave a review</h2>

      {serverError && <div className="alert alert-error" role="alert">{serverError}</div>}

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
