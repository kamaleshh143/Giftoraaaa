<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Orders" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<h1 class="page-title mb-2">Incoming orders</h1>

<c:choose>
    <c:when test="${empty orders}">
        <div class="empty-state">
            <h3>No orders yet</h3>
            <p>Orders containing your products will appear here.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Order</th><th>Ship to</th><th>Date</th><th>Total</th><th>Status</th><th>Update</th></tr></thead>
                <tbody>
                <c:forEach var="o" items="${orders}">
                    <tr>
                        <td><c:out value="${o.orderNumber}" /></td>
                        <td><c:out value="${o.shippingName}" /></td>
                        <td><fmt:formatDate value="${o.createdAtDate}" pattern="dd MMM yyyy" /></td>
                        <td>Rs.<fmt:formatNumber value="${o.total}" minFractionDigits="2" /></td>
                        <td><span class="badge">${o.status}</span></td>
                        <td>
                            <form method="post" action="${ctx}/seller/orders" class="inline-form">
                                <input type="hidden" name="orderId" value="${o.id}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <select name="status" class="form-control">
                                    <option value="CONFIRMED">CONFIRMED</option>
                                    <option value="SHIPPED">SHIPPED</option>
                                    <option value="DELIVERED">DELIVERED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                                <button class="btn btn-primary btn-sm" type="submit">Save</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>

