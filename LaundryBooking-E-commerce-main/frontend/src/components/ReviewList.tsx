import React, { useEffect, useState } from 'react';
import { Review, ReviewSummary } from '../types/review';
import { reviewService } from '../services/reviewService';
import { Rating } from './Rating';
import { ReviewForm } from './ReviewForm';

interface ReviewListProps {
  currentUserId?: string;
}

export const ReviewList: React.FC<ReviewListProps> = ({ currentUserId }) => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [summary, setSummary] = useState<ReviewSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadReviewData();
  }, []);

  const loadReviewData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [reviewsData, summaryData] = await Promise.all([
        reviewService.fetchReviews(),
        reviewService.fetchReviewSummary()
      ]);
      setReviews(reviewsData);
      setSummary(summaryData);
    } catch (err: any) {
      setError(err.message || 'Failed to load reviews');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateReview = async (payload: { rating: number; comment: string }) => {
    const newReview = await reviewService.createReview(payload);
    setReviews((prev) => [newReview, ...prev]);
    const updatedSummary = await reviewService.fetchReviewSummary();
    setSummary(updatedSummary);
  };

  const handleDeleteReview = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this review?')) return;

    try {
      await reviewService.deleteReview(id);
      setReviews((prev) => prev.filter((r) => r.id !== id));
      const updatedSummary = await reviewService.fetchReviewSummary();
      setSummary(updatedSummary);
    } catch (err: any) {
      alert(err.message || 'Failed to delete review');
    }
  };

  if (loading) return <div className="p-6 text-center text-gray-500">Loading reviews...</div>;
  if (error) return <div className="p-6 text-center text-red-600">{error}</div>;

  return (
    <div className="max-w-3xl mx-auto p-4 space-y-6">
      {summary && (
        <div className="flex items-center gap-6 p-4 bg-white shadow rounded-lg">
          <div className="text-center border-r pr-6">
            <div className="text-4xl font-bold text-gray-900">
              {summary.averageRating.toFixed(1)}
            </div>
            <Rating value={Math.round(summary.averageRating)} readOnly size="sm" />
            <div className="text-xs text-gray-500 mt-1">{summary.totalReviews} reviews</div>
          </div>
          <div className="flex-1 space-y-1">
            {([5, 4, 3, 2, 1] as const).map((star) => {
              const count = summary.ratingDistribution[star] || 0;
              const percentage = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
              return (
                <div key={star} className="flex items-center text-xs text-gray-600 gap-2">
                  <span className="w-3">{star}★</span>
                  <div className="flex-1 bg-gray-200 h-2 rounded-full overflow-hidden">
                    <div className="bg-amber-400 h-full" style={{ width: `${percentage}%` }} />
                  </div>
                  <span className="w-8 text-right">{count}</span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      <ReviewForm onSubmit={handleCreateReview} />

      <div className="space-y-4">
        <h3 className="text-lg font-bold text-gray-800">Student Feedback</h3>
        {reviews.length === 0 ? (
          <p className="text-gray-500 text-sm">No reviews yet. Be the first to leave one!</p>
        ) : (
          reviews.map((review) => (
            <div key={review.id} className="p-4 bg-white shadow-sm rounded-lg border border-gray-100">
              <div className="flex justify-between items-start">
                <div>
                  <span className="font-semibold text-gray-900 text-sm">{review.userName}</span>
                  <div className="mt-1">
                    <Rating value={review.rating} readOnly size="sm" />
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-xs text-gray-400">
                    {new Date(review.createdAt).toLocaleDateString()}
                  </span>
                  {currentUserId === review.userId && (
                    <button
                      onClick={() => handleDeleteReview(review.id)}
                      className="text-xs text-red-500 hover:text-red-700 font-medium"
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>
              <p className="mt-2 text-sm text-gray-700">{review.comment}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
};