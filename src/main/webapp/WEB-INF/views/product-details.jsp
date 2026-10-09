<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- product variable is "product" --%>
<c:set var="pageTitle" value="${product.name}" />
<%@ include file="_header.jspf" %>

<div class="card mb-3" style="flex-direction:row;flex-wrap:wrap">
    <img src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.name}'/>" style="width:100%;max-width:420px;height:auto;object-fit:cover" />
    <div class="card-body" style="flex:1;min-width:280px;padding:1.5rem">
        <span class="card-category"><c:out value="${product.category}" /></span>
        <h1 class="page-title">${product.name}</h1>
        <p class="stars mb-1">
            <c:forEach begin="1" end="5" var="i">${i <= product.averageRating ? '★' : '☆'}</c:forEach>
            <span class="text-muted">${product.reviewCount} review(s)</span>
        </p>
        <p class="mb-2"><c:out value="${product.description}" /></p>
        <p class="card-price" style="font-size:1.6rem">Rs.<fmt:formatNumber value="${product.price}" minFractionDigits="2" /></p>
        <c:choose>
            <c:when test="${product.available}">
                <p class="badge badge-active mb-2">In stock (${product.stock} available)</span></p>
            </c:when>
            <c:otherwise><p class="mb-2"><span class="badge badge-inactive">Out of stock</span></p></c:otherwise>
        </c:choose>

        <form method="post" action="${ctx}/cart" class="flex-between" style="max-width:320px">
            <input type="hidden" name="action" value="add" />
            <input type="hidden" name="productId" value="${product.id}" />
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <input class="form-control" type="number" name="quantity" value="1" min="1" max="20" ${product.available ? '' : 'disabled'} />
            <button type="submit" class="btn btn-primary" ${product.available ? '' : 'disabled'}>Add to Cart</button>
        </form>
        <a href="${ctx}/products" class="btn btn-outline mt-2">Back to shop</a>
    </div>
</div>

<section class="section">
    <h2 class="page-title" style="font-size:1.35rem">Customer reviews</h2>
    <c:choose>
        <c:when test="${empty reviews}">
            <p class="text-muted">No reviews yet. Be the first to review after your order is delivered.</p>
        </c:when>
        <c:otherwise>
            <div class="grid" style="grid-template-columns:1fr">
                <c:forEach var="review" items="${reviews}">
                    <div class="card"><div class="card-body">
                        <div class="flex-between">
                            <strong><c:out value="${review.userName}" /></strong>
                            <span class="stars"><c:forEach begin="1" end="5" var="i">${i <= review.rating ? '★' : '☆'}</c:forEach></span>
                        </div>
                        <p class="card-text"><c:out value="${review.comment}" /></p>
                    </div></div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${canReview}">
        <div class="form-card mt-3" style="margin-left:0">
            <h3 class="mb-1">Write a review</h3>
            <form method="post" action="${ctx}/review">
                <input type="hidden" name="productId" value="${product.id}" />
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <div class="form-group">
                    <label for="rating">Rating</label>
                    <select class="form-control" id="rating" name="rating" required>
                        <option value="5">5 - Excellent</option>
                        <option value="4">4 - Very good</option>
                        <option value="3">3 - Average</option>
                        <option value="2">2 - Poor</option>
                        <option value="1">1 - Terrible</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="comment">Your review</label>
                    <textarea class="form-control" id="comment" name="comment" maxlength="1500" required></textarea>
                </div>
                <button type="submit" class="btn btn-primary">Submit review</button>
            </form>
        </div>
    </c:if>
</section>

<%@ include file="_footer.jspf" %>
