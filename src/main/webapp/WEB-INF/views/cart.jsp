<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Your Cart" />
<%@ include file="_header.jspf" %>

<h1 class="page-title mb-2">Your cart</h1>

<c:choose>
    <c:when test="${empty cartItems}">
        <div class="empty-state">
            <h3>Your cart is empty</h3>
            <p>Add some gifts to get started.</p>
            <a href="${ctx}/products" class="btn btn-primary mt-2">Start shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="grid" style="grid-template-columns:2fr 1fr;align-items:start">
            <div>
                <c:forEach var="item" items="${cartItems}">
                    <div class="cart-row">
                        <img src="<c:out value='${item.imageUrl}'/>" alt="<c:out value='${item.name}'/>" />
                        <div>
                            <a href="${ctx}/product?id=${item.productId}"><strong><c:out value="${item.name}" /></strong></a>
                            <div class="text-muted">Rs.<fmt:formatNumber value="${item.price}" minFractionDigits="2" /> each</div>
                        </div>
                        <form method="post" action="${ctx}/cart" class="qty-form">
                            <input type="hidden" name="action" value="update" />
                            <input type="hidden" name="productId" value="${item.productId}" />
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                            <input class="form-control" type="number" name="quantity" value="${item.quantity}" min="0" max="20" />
                            <button type="submit" class="btn btn-outline btn-sm">Update</button>
                        </form>
                        <div class="cart-actions text-center">
                            <strong>Rs.<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" /></strong>
                            <form method="post" action="${ctx}/cart" class="mt-1">
                                <input type="hidden" name="action" value="remove" />
                                <input type="hidden" name="productId" value="${item.productId}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button type="submit" class="btn btn-danger btn-sm" data-confirm="Remove this item from your cart?">Remove</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <aside class="cart-summary">
                <h3 class="mb-2">Order summary</h3>
                <div class="summary-line"><span>Items</span><span>${cartCount}</span></div>
                <div class="summary-line total"><span>Total</span><span>Rs.<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" /></span></div>
                <a href="${ctx}/checkout" class="btn btn-primary btn-block mt-2">Proceed to checkout</a>
                <a href="${ctx}/products" class="btn btn-outline btn-block mt-1">Continue shopping</a>
            </aside>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_footer.jspf" %>
