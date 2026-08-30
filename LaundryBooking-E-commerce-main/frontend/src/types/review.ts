export interface Review {
  id: string;
  userId: string;
  userName: string;
  userAvatarUrl?: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt?: string;
  orderId?: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<1 | 2 | 3 | 4 | 5, number>;
}

export interface CreateReviewPayload {
  rating: number;
  comment: string;
  orderId?: string;
}

export interface UpdateReviewPayload {
  rating?: number;
  comment?: string;
}