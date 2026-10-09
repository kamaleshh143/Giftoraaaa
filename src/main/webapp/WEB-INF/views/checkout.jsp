<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Checkout" />
<%@ include file="_header.jspf" %>

<h1 class="page-title mb-2">Checkout</h1>
<p class="page-subtitle">This is a demo store. No real payment is taken.</p>

<div class="grid" style="grid-template-columns:2fr 1fr;align-items:start">
    <div class="form-card" style="margin:0;max-width:none">
        <h3 class="mb-2">Shipping details</h3>
        <form method="post" action="${ctx}/checkout">
            <input type="hidden" name="checkoutToken" value="${checkoutToken}" />
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <div class="form-group">
                <label for="shippingName">Recipient name</label>
                <input class="form-control" type="text" id="shippingName" name="shippingName" value="<c:out value='${shippingName}'/>" required />
            </div>
            <div class="form-group">
                <label for="shippingAddress">Shipping address</label>
                <textarea class="form-control" id="shippingAddress" name="shippingAddress" required><c:out value="${shippingAddress}" /></textarea>
            </div>
            <div class="form-group">
                <label for="shippingPhone">Phone number</label>
                <input class="form-control" type="text" id="shippingPhone" name="shippingPhone" value="<c:out value='${shippingPhone}'/>" required />
            </div>
            <div class="alert alert-info">Payment method: <strong>Mock payment (demo)</strong>. No card details are collected.</div>
            <button type="submit" class="btn btn-primary btn-block">Place order</button>
        </form>
    </div>
    <aside class="cart-summary">
        <h3 class="mb-2">Your order</h3>
        <c:forEach var="item" items="${cartItems}">
            <div class="summary-line">
                <span><c:out value="${item.name}" /> &times; ${item.quantity}</span>
                <span>Rs.<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" /></span>
            </div>
        </c:forEach>
        <div class="summary-line total"><span>Total</span><span>Rs.<fmt:formatNumber value="${cartTotal}" minFractionDigits="2" /></span></div>
        <a href="${ctx}/cart" class="btn btn-outline btn-block mt-1">Back to cart</a>
    </aside>
</div>

<%@ include file="_footer.jspf" %>
