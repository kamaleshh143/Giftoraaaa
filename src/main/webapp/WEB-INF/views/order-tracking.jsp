<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Track Order" />
<%@ include file="_header.jspf" %>

<div class="flex-between mb-2">
    <div>
        <h1 class="page-title">Order <c:out value="${order.orderNumber}" /></h1>
        <p class="page-subtitle">Placed on <fmt:formatDate value="${order.createdAtDate}" pattern="dd MMM yyyy, HH:mm" /></p>
    </div>
    <span class="badge badge-${order.status}">${order.status}</span>
</div>

<c:if test="${order.status == 'CANCELLED'}">
    <div class="alert alert-error">This order was cancelled.</div>
</c:if>

<div class="grid" style="grid-template-columns:1fr 1fr;align-items:start">
    <div class="card"><div class="card-body">
        <h3 class="mb-2">Tracking timeline</h3>
        <div class="timeline">
            <div class="timeline-step ${order.timelineIndex >= 0 ? 'done' : ''}">
                <h4>Order placed</h4><p>We received your order.</p>
            </div>
            <div class="timeline-step ${order.timelineIndex >= 1 ? 'done' : ''}">
                <h4>Confirmed</h4><p>Your order is confirmed by the seller.</p>
            </div>
            <div class="timeline-step ${order.timelineIndex >= 2 ? 'done' : ''}">
                <h4>Shipped</h4><p>Your parcel is on the way.</p>
            </div>
            <div class="timeline-step ${order.timelineIndex >= 3 ? 'done' : ''}">
                <h4>Delivered</h4><p>Enjoy your purchase!</p>
            </div>
        </div>
    </div></div>

    <div class="card"><div class="card-body">
        <h3 class="mb-2">Items</h3>
        <c:forEach var="item" items="${order.items}">
            <div class="summary-line">
                <span><a href="${ctx}/product?id=${item.productId}"><c:out value="${item.productName}" /></a> &times; ${item.quantity}</span>
                <span>Rs.<fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" /></span>
            </div>
        </c:forEach>
        <div class="summary-line total"><span>Total</span><span>Rs.<fmt:formatNumber value="${order.total}" minFractionDigits="2" /></span></div>
        <h4 class="mt-2 mb-1">Shipping to</h4>
        <p class="text-muted"><c:out value="${order.shippingName}" /><br /><c:out value="${order.shippingAddress}" /><br /><c:out value="${order.shippingPhone}" /></p>

        <c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED'}">
            <form method="post" action="${ctx}/order/cancel" class="mt-2">
                <input type="hidden" name="orderId" value="${order.id}" />
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <button type="submit" class="btn btn-danger btn-block" data-confirm="Cancel this order?">Cancel order</button>
            </form>
        </c:if>
        <a href="${ctx}/orders" class="btn btn-outline btn-block mt-1">Back to orders</a>
    </div></div>
</div>

<%@ include file="_footer.jspf" %>

