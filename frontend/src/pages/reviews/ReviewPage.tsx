import React from "react";
import { ReviewList } from "../../components/review/ReviewList";
import { getStudentId } from "../../services/session";

export const ReviewsPage: React.FC = () => {
  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Reviews</h1>
          <p>See what other students say about the laundry services, and add your own.</p>
        </div>
      </header>
      <ReviewList currentUserId={getStudentId()} />
    </div>
  );
};

export default ReviewsPage;
