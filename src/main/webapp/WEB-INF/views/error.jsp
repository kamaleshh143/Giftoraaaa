<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Error" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="empty-state">
    <h1 class="page-title">Something went wrong</h1>
    <c:choose>
        <c:when test="${not empty errorMessage}">
            <p><c:out value="${errorMessage}" /></p>
        </c:when>
        <c:otherwise>
            <p>We could not complete that request. Please try again.</p>
        </c:otherwise>
    </c:choose>
    <p class="mt-2">
        <a class="btn btn-primary" href="${ctx}/">Back to home</a>
        <a class="btn btn-outline" href="${ctx}/products">Browse products</a>
    </p>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
