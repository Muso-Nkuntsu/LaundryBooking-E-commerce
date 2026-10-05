import { apiDelete, apiGet, apiPost } from './Api';
import type { Review, ReviewSummary, CreateReviewPayload } from '../types/review';

// Shape sent by the backend: the student and the service are nested objects.
interface RawReview {
  id: number;
  rating: number;
  comment: string;
  date: string;
  student?: { studentId: number; firstName?: string; lastName?: string };
  laundryService?: { id: number; serviceName?: string };
}

const toReview = (raw: RawReview): Review => {
  const first = raw.student?.firstName ?? 'Student';
  const lastInitial = raw.student?.lastName ? ` ${raw.student.lastName[0]}.` : '';
  return {
    id: raw.id,
    userId: raw.student?.studentId ?? 0,
    userName: `${first}${lastInitial}`,
    rating: raw.rating,
    comment: raw.comment,
    createdAt: raw.date,
    serviceId: raw.laundryService?.id,
    serviceName: raw.laundryService?.serviceName,
  };
};

export const reviewService = {
  async fetchReviews(): Promise<Review[]> {
    return (await apiGet<RawReview[]>('/review/getall')).map(toReview);
  },

  fetchReviewSummary(): Promise<ReviewSummary> {
    return apiGet<ReviewSummary>('/review/summary');
  },

  async createReview(payload: CreateReviewPayload): Promise<Review> {
    return toReview(await apiPost<RawReview>('/review/create', payload));
  },

  async deleteReview(id: number): Promise<void> {
    await apiDelete(`/review/delete/${id}`);
  },
};
