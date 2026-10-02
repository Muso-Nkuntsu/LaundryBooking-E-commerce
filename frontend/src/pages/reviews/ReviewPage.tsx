import React from "react";
import { ReviewList } from "../../components/review/ReviewList";
import { getSession } from "../../services/session";

export const ReviewsPage: React.FC = () => {
  // Reviews written by this student get a Delete button.
  const studentId = getSession()?.studentId;
  const currentUserId = studentId !== undefined ? String(studentId) : undefined;

  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Reviews</h1>
          <p>See what other students say about the laundry service, and add your own.</p>
        </div>
      </header>
      <ReviewList currentUserId={currentUserId} />
    </div>
  );
};

export default ReviewsPage;
