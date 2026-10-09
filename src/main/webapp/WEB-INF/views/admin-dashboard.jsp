<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Admin Dashboard" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<h1 class="page-title mb-2">Admin dashboard</h1>
<p class="page-subtitle">Overview of the Giftora marketplace.</p>

<div class="grid grid-stats mb-3">
    <a class="stat-card" href="${ctx}/admin/users"><div class="stat-value">${userCount}</div><div class="stat-label">Users</div></a>
    <a class="stat-card" href="${ctx}/admin/products"><div class="stat-value">${productCount}</div><div class="stat-label">Products</div></a>
    <a class="stat-card" href="${ctx}/admin/orders"><div class="stat-value">${orderCount}</div><div class="stat-label">Orders</div></a>
    <a class="stat-card" href="${ctx}/admin/reviews"><div class="stat-value">${reviewCount}</div><div class="stat-label">Reviews</div></a>
</div>

<h3 class="mb-1">Manage</h3>
<p>
    <a class="btn btn-outline" href="${ctx}/admin/users">Users</a>
    <a class="btn btn-outline" href="${ctx}/admin/products">Products</a>
    <a class="btn btn-outline" href="${ctx}/admin/orders">Orders</a>
    <a class="btn btn-outline" href="${ctx}/admin/reviews">Reviews</a>
</p>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
