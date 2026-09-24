import { Review, ReviewSummary, CreateReviewPayload, UpdateReviewPayload } from '../types/review';

const BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  return response.json();
}

export const reviewService = {
  async fetchReviews(): Promise<Review[]> {
    const res = await fetch(`${BASE_URL}/reviews`);
    return handleResponse<Review[]>(res);
  },

  async fetchReviewSummary(): Promise<ReviewSummary> {
    const res = await fetch(`${BASE_URL}/reviews/summary`);
    return handleResponse<ReviewSummary>(res);
  },

  async createReview(payload: CreateReviewPayload): Promise<Review> {
    const res = await fetch(`${BASE_URL}/reviews`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return handleResponse<Review>(res);
  },

  async updateReview(id: string, payload: UpdateReviewPayload): Promise<Review> {
    const res = await fetch(`${BASE_URL}/reviews/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return handleResponse<Review>(res);
  },

  async deleteReview(id: string): Promise<void> {
    const res = await fetch(`${BASE_URL}/reviews/${id}`, { method: 'DELETE' });
    return handleResponse<void>(res);
  }
};