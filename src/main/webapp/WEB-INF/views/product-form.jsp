<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="editing" value="${not empty product}" />
<c:set var="pageTitle" value="${editing ? 'Edit Product' : 'Add Product'}" />
<%@ include file="_header.jspf" %>

<h1 class="page-title mb-2">${editing ? 'Edit product' : 'Add a new product'}</h1>

<div class="form-card" style="margin:0;max-width:640px">
    <form method="post" action="${ctx}/seller/product">
        <c:if test="${editing}"><input type="hidden" name="id" value="${product.id}" /></c:if>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
        <div class="form-group">
            <label for="name">Product name</label>
            <input class="form-control" type="text" id="name" name="name" value="<c:out value='${product.name}'/>" required maxlength="150" />
        </div>
        <div class="form-group">
            <label for="description">Description</label>
            <textarea class="form-control" id="description" name="description" rows="4" maxlength="2000"><c:out value="${product.description}" /></textarea>
        </div>
        <div class="form-group">
            <label for="category">Category</label>
            <select class="form-control" id="category" name="category" required>
                <option value="">Select a category</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="<c:out value='${cat}'/>" ${product.category == cat ? 'selected' : ''}><c:out value="${cat}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="form-group">
            <label for="price">Price (Rs.)</label>
            <input class="form-control" type="number" step="0.01" min="0.01" id="price" name="price" value="${product.price}" required />
        </div>
        <div class="form-group">
            <label for="stock">Stock quantity</label>
            <input class="form-control" type="number" min="0" id="stock" name="stock" value="${empty product ? 0 : product.stock}" required />
        </div>
        <div class="form-group">
            <label for="imageUrl">Image URL (optional)</label>
            <input class="form-control" type="text" id="imageUrl" name="imageUrl" value="<c:out value='${product.imageUrl}'/>" maxlength="500" />
        </div>
        <c:if test="${editing}">
            <div class="form-group">
                <label><input type="checkbox" name="active" value="true" ${product.active ? 'checked' : ''} /> Visible to buyers</label>
            </div>
        </c:if>
        <button class="btn btn-primary" type="submit">${editing ? 'Save changes' : 'Add product'}</button>
        <a class="btn btn-outline" href="${ctx}/seller/products">Cancel</a>
    </form>
</div>

<%@ include file="_footer.jspf" %>
