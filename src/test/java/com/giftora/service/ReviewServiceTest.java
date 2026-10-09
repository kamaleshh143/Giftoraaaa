package com.giftora.service;

import com.giftora.dao.ProductDao;
import com.giftora.dao.ReviewDao;
import com.giftora.exception.AuthorizationException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Review;
import com.giftora.model.Role;
import com.giftora.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private ReviewDao reviewDao;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewDao = mock(ReviewDao.class);
        reviewService = new ReviewService(reviewDao, mock(ProductDao.class));
    }

    private User buyer() {
        User u = new User("Buyer One", "buyer@x.com", "hash", Role.BUYER);
        u.setId(10);
        return u;
    }

    @Test
    void reviewRequiresDeliveredPurchase() throws Exception {
        when(reviewDao.hasDeliveredPurchase(10, 1)).thenReturn(false);
        assertThrows(AuthorizationException.class, () -> reviewService.submitReview(buyer(), 1, 5, "Great gift!"));
        verify(reviewDao, never()).insert(any());
    }

    @Test
    void cannotReviewSameProductTwice() throws Exception {
        when(reviewDao.hasDeliveredPurchase(10, 1)).thenReturn(true);
        when(reviewDao.existsForUser(1, 10)).thenReturn(true);
        assertThrows(ValidationException.class, () -> reviewService.submitReview(buyer(), 1, 5, "Great gift!"));
    }

    @Test
    void rejectsOutOfRangeRating() {
        assertThrows(ValidationException.class, () -> reviewService.submitReview(buyer(), 1, 0, "Great gift!"));
        assertThrows(ValidationException.class, () -> reviewService.submitReview(buyer(), 1, 6, "Great gift!"));
    }

    @Test
    void rejectsTooShortComment() {
        assertThrows(ValidationException.class, () -> reviewService.submitReview(buyer(), 1, 5, "ok"));
    }

    @Test
    void successfulReviewIsStoredApproved() throws Exception {
        when(reviewDao.hasDeliveredPurchase(10, 1)).thenReturn(true);
        when(reviewDao.existsForUser(1, 10)).thenReturn(false);

        Review result = reviewService.submitReview(buyer(), 1, 5, "  Great gift!  ");
        assertEquals("Great gift!", result.getComment());
        assertTrue(result.isApproved());

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewDao).insert(captor.capture());
        assertEquals(1L, captor.getValue().getProductId());
        assertEquals(10L, captor.getValue().getUserId());
    }

    @Test
    void anonymousReviewIsRejected() throws Exception {
        assertThrows(AuthorizationException.class, () -> reviewService.submitReview(null, 1, 5, "Great gift!"));
        verify(reviewDao, never()).insert(any());
    }
}
