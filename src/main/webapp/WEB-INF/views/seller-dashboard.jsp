<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Seller Dashboard" />
<%@ include file="_header.jspf" %>

<div class="flex-between mb-2">
    <div>
        <h1 class="page-title">Seller dashboard</h1>
        <p class="page-subtitle">Manage your products and orders.</p>
    </div>
    <a href="${ctx}/seller/product" class="btn btn-primary">Add product</a>
</div>

<div class="grid grid-stats mb-3">
    <div class="stat-card"><div class="stat-value">${productCount}</div><div class="stat-label">Products</div></div>
    <div class="stat-card"><div class="stat-value">${lowStockCount}</div><div class="stat-label">Low stock (5 or fewer)</div></div>
</div>

<div class="flex-between mb-1">
    <h3>Your recent products</h3>
    <a href="${ctx}/seller/products">Manage all products</a>
</div>
<c:choose>
    <c:when test="${empty products}">
        <div class="empty-state">
            <h3>No products yet</h3>
            <p>Add your first product to start selling.</p>
            <a href="${ctx}/seller/product" class="btn btn-primary mt-2">Add product</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td><c:out value="${p.name}" /></td>
                        <td><c:out value="${p.category}" /></td>
                        <td>Rs.<fmt:formatNumber value="${p.price}" minFractionDigits="2" /></td>
                        <td>${p.stock}</td>
                        <td><span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Active' : 'Hidden'}</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<p class="mt-3"><a href="${ctx}/seller/orders" class="btn btn-outline">Manage orders</a></p>

<%@ include file="_footer.jspf" %>
