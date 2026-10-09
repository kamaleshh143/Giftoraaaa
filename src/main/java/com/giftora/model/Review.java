package com.giftora.model;

import java.io.Serializable;
import java.time.Instant;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long productId;
    private long userId;
    private String userName;
    private int rating;
    private String comment;
    private boolean approved;
    private Instant createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Date view for JSTL {@code <fmt:formatDate>}, which cannot format {@link Instant} directly.
     */
    public java.util.Date getCreatedAtDate() {
        return createdAt == null ? null : java.util.Date.from(createdAt);
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
