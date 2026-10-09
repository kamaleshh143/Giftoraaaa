<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Orders" />
<%@ include file="_header.jspf" %>

<h1 class="page-title mb-2">My orders</h1>

<c:choose>
    <c:when test="${empty orders}">
        <div class="empty-state">
            <h3>No orders yet</h3>
            <p>When you place an order it will appear here.</p>
            <a href="${ctx}/products" class="btn btn-primary mt-2">Start shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="table">
                <thead>
                <tr><th>Order</th><th>Date</th><th>Items</th><th>Total</th><th>Status</th><th></th></tr>
                </thead>
                <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td><strong><c:out value="${order.orderNumber}" /></strong></td>
                        <td><fmt:formatDate value="${order.createdAtDate}" pattern="dd MMM yyyy" /></td>
                        <td>${order.items.size()}</td>
                        <td>Rs.<fmt:formatNumber value="${order.total}" minFractionDigits="2" /></td>
                        <td><span class="badge badge-${order.status}">${order.status}</span></td>
                        <td><a href="${ctx}/order/track?id=${order.id}" class="btn btn-outline btn-sm">Track</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_footer.jspf" %>


