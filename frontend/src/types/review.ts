export interface Review {
  id: number;
  userId: number;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
  serviceId?: number;
  serviceName?: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<1 | 2 | 3 | 4 | 5, number>;
}

export interface CreateReviewPayload {
  studentId: number;
  serviceId: number;
  rating: number;
  comment: string;
}
