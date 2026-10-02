import React, { useState } from "react";
import type { Review, ReviewSummary } from "../../types/review";
import { reviewService } from "../../services/reviewServices";
import { Rating } from "./Rating";
import { ReviewForm } from "./ReviewForm";
import ConfirmDialog from "../common/ConfirmDialog";
import { ErrorState, Loading } from "../common/States";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { friendlyError } from "../../utilis/errorMessage";

interface ReviewListProps {
  currentUserId?: string;
}

interface ReviewData {
  reviews: Review[];
  summary: ReviewSummary | null;
}

export const ReviewList: React.FC<ReviewListProps> = ({ currentUserId }) => {
  const toast = useToast();
  const [toDelete, setToDelete] = useState<Review | null>(null);
  const [deleting, setDeleting] = useState(false);

  const { data, loading, error, reload, setData } = useFetch<ReviewData>(async () => {
    try {
      const [reviews, summary] = await Promise.all([reviewService.fetchReviews(), reviewService.fetchReviewSummary()]);
      return { reviews: Array.isArray(reviews) ? reviews : [], summary };
    } catch (err) {
      throw new Error(friendlyError(err, "We couldn't load the reviews."));
    }
  });

  const refreshSummary = async () => {
    const summary = await reviewService.fetchReviewSummary();
    setData((prev) => (prev ? { ...prev, summary } : prev));
  };

  const handleCreateReview = async (payload: { rating: number; comment: string }) => {
    const newReview = await reviewService.createReview(payload);
    setData((prev) => (prev ? { ...prev, reviews: [newReview, ...prev.reviews] } : prev));
    toast.success("Review posted.");
    await refreshSummary().catch(() => undefined);
  };

  const handleDeleteReview = async () => {
    if (!toDelete) return;
    try {
      setDeleting(true);
      await reviewService.deleteReview(toDelete.id);
      setData((prev) => (prev ? { ...prev, reviews: prev.reviews.filter((r) => r.id !== toDelete.id) } : prev));
      toast.success("Review deleted.");
      setToDelete(null);
      await refreshSummary().catch(() => undefined);
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't delete that review."));
    } finally {
      setDeleting(false);
    }
  };

  if (loading) return <Loading message="Loading reviews..." />;
  if (error || !data) return <ErrorState message={error ?? "We couldn't load the reviews."} onRetry={reload} />;

  const { reviews, summary } = data;

  return (
    <div className="stack">
      {summary && (
        <section className="card row" style={{ flexWrap: "wrap", gap: 24 }}>
          <div style={{ textAlign: "center" }}>
            <div className="score">{summary.averageRating.toFixed(1)}</div>
            <Rating value={Math.round(summary.averageRating)} readOnly size="sm" />
            <div className="small muted">{summary.totalReviews} reviews</div>
          </div>
          <div className="bars">
            {([5, 4, 3, 2, 1] as const).map((star) => {
              const count = summary.ratingDistribution?.[star] || 0;
              const percentage = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
              return (
                <div key={star} className="bar">
                  <span>{star}★</span>
                  <i><b style={{ width: `${percentage}%` }} /></i>
                  <span style={{ textAlign: "right" }}>{count}</span>
                </div>
              );
            })}
          </div>
        </section>
      )}

      <ReviewForm onSubmit={handleCreateReview} />

      <h2 style={{ marginTop: 8 }}>What students say</h2>
      {reviews.length === 0 ? (
        <p className="muted">No reviews yet. Yours can be the first.</p>
      ) : (
        reviews.map((review) => (
          <article key={review.id} className="card" style={{ margin: 0 }}>
            <div className="row" style={{ alignItems: "flex-start" }}>
              <div>
                <strong>{review.userName}</strong>
                <div><Rating value={review.rating} readOnly size="sm" /></div>
              </div>
              <div className="row small muted">
                {new Date(review.createdAt).toLocaleDateString("en-ZA", { dateStyle: "medium" })}
                {currentUserId === review.userId && (
                  <button type="button" className="link-btn" style={{ color: "var(--peg)" }} onClick={() => setToDelete(review)}>
                    Delete
                  </button>
                )}
              </div>
            </div>
            <p style={{ marginTop: 10 }}>{review.comment}</p>
          </article>
        ))
      )}

      {toDelete && (
        <ConfirmDialog
          title="Delete your review?"
          message="This removes it for everyone and can't be undone."
          confirmLabel="Delete review"
          busy={deleting}
          onConfirm={handleDeleteReview}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
};
