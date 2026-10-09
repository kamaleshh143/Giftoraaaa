<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Manage Reviews" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<h1 class="page-title mb-2">Reviews</h1>

<c:choose>
    <c:when test="${empty reviews}">
        <div class="empty-state"><h3>No reviews yet</h3></div>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="table">
                <thead><tr><th>Product</th><th>Reviewer</th><th>Rating</th><th>Comment</th><th>Date</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                <c:forEach var="r" items="${reviews}">
                    <tr>
                        <td><a href="${ctx}/product?id=${r.productId}">#${r.productId}</a></td>
                        <td><c:out value="${r.userName}" /></td>
                        <td>${r.rating}/5</td>
                        <td class="comment-cell"><c:out value="${r.comment}" /></td>
                        <td><fmt:formatDate value="${r.createdAtDate}" pattern="dd MMM yyyy" /></td>
                        <td><span class="badge ${r.approved ? 'badge-active' : 'badge-inactive'}">${r.approved ? 'Approved' : 'Hidden'}</span></td>
                        <td>
                            <form method="post" action="${ctx}/admin/reviews" class="inline-form">
                                <input type="hidden" name="action" value="${r.approved ? 'reject' : 'approve'}" />
                                <input type="hidden" name="reviewId" value="${r.id}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button class="btn btn-outline btn-sm" type="submit">${r.approved ? 'Hide' : 'Approve'}</button>
                            </form>
                            <form method="post" action="${ctx}/admin/reviews" class="inline-form">
                                <input type="hidden" name="action" value="delete" />
                                <input type="hidden" name="reviewId" value="${r.id}" />
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button class="btn btn-danger btn-sm" type="submit" data-confirm="Delete this review?">Delete</button>
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


