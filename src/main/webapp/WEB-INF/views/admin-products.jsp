<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Manage Products" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<h1 class="page-title mb-2">Products</h1>

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
                    <form method="post" action="${ctx}/admin/products" class="inline-form">
                        <input type="hidden" name="action" value="toggle" />
                        <input type="hidden" name="productId" value="${p.id}" />
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <button class="btn btn-outline btn-sm" type="submit">${p.active ? 'Hide' : 'Show'}</button>
                    </form>
                    <form method="post" action="${ctx}/admin/products" class="inline-form">
                        <input type="hidden" name="action" value="delete" />
                        <input type="hidden" name="productId" value="${p.id}" />
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <button class="btn btn-danger btn-sm" type="submit" data-confirm="Delete this product permanently?">Delete</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
