<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Products" />
<%@ include file="_header.jspf" %>

<div class="flex-between mb-2">
    <h1 class="page-title">My products</h1>
    <a href="${ctx}/seller/product" class="btn btn-primary">Add product</a>
</div>

<c:choose>
    <c:when test="${empty products}">
        <div class="empty-state"><h3>No products yet</h3><p>Add your first product.</p></div>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td><c:out value="${p.name}" /></td>
                        <td><c:out value="${p.category}" /></td>
                        <td>Rs.<fmt:formatNumber value="${p.price}" minFractionDigits="2" /></td>
                        <td>${p.stock}</td>
                        <td><span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Active' : 'Hidden'}</span></td>
                        <td>
                            <a href="${ctx}/seller/product?id=${p.id}" class="btn btn-outline btn-sm">Edit</a>
                            <form method="post" action="${ctx}/seller/products" class="inline-form">
                                <input type="hidden" name="action" value="toggle" />
                                <input type="hidden" name="id" value="${p.id}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button class="btn btn-outline btn-sm" type="submit">${p.active ? 'Hide' : 'Show'}</button>
                            </form>
                            <form method="post" action="${ctx}/seller/products" class="inline-form">
                                <input type="hidden" name="action" value="delete" />
                                <input type="hidden" name="id" value="${p.id}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button class="btn btn-danger btn-sm" type="submit" data-confirm="Delete this product permanently?">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_footer.jspf" %>

