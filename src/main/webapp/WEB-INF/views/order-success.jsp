<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Order Confirmed" />
<%@ include file="_header.jspf" %>

<div class="form-card wide text-center">
    <h1 class="page-title">Thank you for your order!</h1>
    <p class="page-subtitle">Your order has been placed successfully.</p>
    <div class="alert alert-success">
        Order number: <strong><c:out value="${order.orderNumber}" /></strong>
    </div>
    <p class="mb-3">Total: Rs.<fmt:formatNumber value="${order.total}" minFractionDigits="2" /> &middot;
        Status: <span class="badge badge-${order.status}">${order.status}</span></p>
    <a href="${ctx}/order/track?id=${order.id}" class="btn btn-primary btn-block">Track order</a>
    <a href="${ctx}/orders" class="btn btn-outline btn-block mt-1">View order history</a>
    <a href="${ctx}/products" class="btn btn-outline btn-block mt-1">Continue shopping</a>
</div>

<%@ include file="_footer.jspf" %>
