// src/pages/reviews/ReviewsPage.tsx

import React from 'react';
import { ReviewList } from '../../components/ReviewList';

export const ReviewsPage: React.FC = () => {
  // Pass currentUserId from your auth context or state management solution if available
  const currentUserId = 'user-123';

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      {/* Top Header Section */}
      <div className="max-w-3xl mx-auto mb-6 pb-4 border-b border-gray-200">
        <h1 className="text-2xl font-bold text-gray-900">Laundry Service Reviews</h1>
        <p className="text-sm text-gray-500">
          See what other students are saying and share your own experience
        </p>
      </div>

      {/* Main Review Summary, Form, & List */}
      <main className="max-w-3xl mx-auto">
        <ReviewList currentUserId={currentUserId} />
      </main>
    </div>
  );
};

export default ReviewsPage;