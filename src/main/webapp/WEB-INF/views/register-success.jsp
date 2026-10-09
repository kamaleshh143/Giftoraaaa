<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Welcome" />
<%@ include file="_header.jspf" %>

<div class="form-card text-center">
    <h1 class="page-title">Your account is ready</h1>
    <p class="page-subtitle">Welcome to Giftora, <c:out value="${registeredName}" />!</p>
    <p class="mb-3">You registered as a <span class="badge badge-${registeredRole}">${registeredRole}</span>.</p>
    <a href="${ctx}/login" class="btn btn-primary btn-block">Log in now</a>
    <a href="${ctx}/products" class="btn btn-outline btn-block mt-1">Browse products</a>
</div>

<%@ include file="_footer.jspf" %>
