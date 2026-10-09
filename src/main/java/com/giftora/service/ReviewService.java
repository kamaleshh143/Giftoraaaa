package com.giftora.service;

import com.giftora.dao.ProductDao;
import com.giftora.dao.ReviewDao;
import com.giftora.exception.AuthorizationException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Review;
import com.giftora.model.User;
import com.giftora.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao;
    private final ProductDao productDao;

    public ReviewService() {
        this(new ReviewDao(), new ProductDao());
    }

    public ReviewService(ReviewDao reviewDao, ProductDao productDao) {
        this.reviewDao = reviewDao;
        this.productDao = productDao;
    }

    public List<Review> approvedForProduct(long productId) throws SQLException {
        return reviewDao.findByProduct(productId, true);
    }

    public List<Review> allReviews() throws SQLException {
        return reviewDao.findAll();
    }

    public boolean hasDeliveredPurchase(long userId, long productId) throws SQLException {
        return reviewDao.hasDeliveredPurchase(userId, productId);
    }

    public boolean alreadyReviewed(long userId, long productId) throws SQLException {
        return reviewDao.existsForUser(productId, userId);
    }

    /**
     * Only a verified buyer with a DELIVERED order for the product may review it.
     */
    public Review submitReview(User buyer, long productId, int rating, String comment)
            throws ValidationException, AuthorizationException, SQLException {
        if (buyer == null) {
            throw new AuthorizationException("Please log in to write a review.");
        }
        if (!ValidationUtil.isValidRating(rating)) {
            throw new ValidationException("Rating must be between 1 and 5 stars.");
        }
        if (ValidationUtil.isBlank(comment) || comment.trim().length() < 4) {
            throw new ValidationException("Please write a short review (at least 4 characters).");
        }
        if (!reviewDao.hasDeliveredPurchase(buyer.getId(), productId)) {
            throw new AuthorizationException("You can review this product only after your order is delivered.");
        }
        if (reviewDao.existsForUser(productId, buyer.getId())) {
            throw new ValidationException("You have already reviewed this product.");
        }
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyer.getId());
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setApproved(true);
        reviewDao.insert(review);
        return review;
    }

    public void moderate(long reviewId, boolean approved) throws SQLException {
        reviewDao.setApproved(reviewId, approved);
    }

    public void delete(long reviewId) throws SQLException {
        reviewDao.delete(reviewId);
    }
}
