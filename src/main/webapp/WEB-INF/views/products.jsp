<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Shop" />
<%@ include file="_header.jspf" %>

<div class="flex-between mb-2">
    <div>
        <h1 class="page-title">Shop the collection</h1>
        <p class="page-subtitle">${totalResults} products available</p>
    </div>
</div>

<form class="toolbar" method="get" action="${ctx}/products">
    <div class="form-group">
        <label for="q">Search</label>
        <input class="form-control" type="text" id="q" name="q" value="<c:out value='${query}'/>" placeholder="Search products..." />
    </div>
    <div class="form-group">
        <label for="category">Category</label>
        <select class="form-control" id="category" name="category">
            <option value="">All categories</option>
            <c:forEach var="cat" items="${categories}">
                <option value="<c:out value='${cat}'/>" ${cat == selectedCategory ? 'selected' : ''}><c:out value="${cat}" /></option>
            </c:forEach>
        </select>
    </div>
    <div class="form-group">
        <label for="sort">Sort by</label>
        <select class="form-control" id="sort" name="sort">
            <option value="newest" ${sort == 'newest' ? 'selected' : ''}>Newest</option>
            <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Price: low to high</option>
            <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Price: high to low</option>
            <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Name A-Z</option>
            <option value="rating_desc" ${sort == 'rating_desc' ? 'selected' : ''}>Top rated</option>
        </select>
    </div>
    <div class="form-group">
        <button type="submit" class="btn btn-primary">Apply</button>
        <a href="${ctx}/products" class="btn btn-outline">Reset</a>
    </div>
</form>

<c:choose>
    <c:when test="${empty products}">
        <div class="empty-state">
            <h3>No products found</h3>
            <p>Try a different search term or category.</p>
            <a href="${ctx}/products" class="btn btn-primary mt-2">Clear filters</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="grid grid-products">
            <c:forEach var="product" items="${products}">
                <div class="card">
                    <a href="${ctx}/product?id=${product.id}">
                        <img class="product-img" src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.name}'/>" loading="lazy" />
                    </a>
                    <div class="card-body">
                        <span class="card-category"><c:out value="${product.category}" /></span>
                        <a href="${ctx}/product?id=${product.id}" class="card-title"><c:out value="${product.name}" /></a>
                        <span class="stars">
                            <c:forEach begin="1" end="5" var="i">${i <= product.averageRating ? '★' : '☆'}</c:forEach>
                            <span class="text-muted">(${product.reviewCount})</span>
                        </span>
                        <span class="card-price">Rs.<fmt:formatNumber value="${product.price}" minFractionDigits="2" /></span>
                        <c:choose>
                            <c:when test="${product.available}"><span class="badge badge-active">In stock</span></c:when>
                            <c:otherwise><span class="badge badge-inactive">Out of stock</span></c:otherwise>
                        </c:choose>
                        <div class="card-actions">
                            <a href="${ctx}/product?id=${product.id}" class="btn btn-outline btn-sm">View</a>
                            <c:if test="${product.available}">
                                <form method="post" action="${ctx}/cart" class="inline-form">
                                    <input type="hidden" name="action" value="add" />
                                    <input type="hidden" name="productId" value="${product.id}" />
                                    <input type="hidden" name="quantity" value="1" />
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                    <button type="submit" class="btn btn-primary btn-sm">Add to Cart</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <c:choose>
                        <c:when test="${p == page}">
                            <span class="current">${p}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/products?page=${p}&q=<c:out value='${query}'/>&category=<c:out value='${selectedCategory}'/>&sort=${sort}">${p}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>

<%@ include file="_footer.jspf" %>
