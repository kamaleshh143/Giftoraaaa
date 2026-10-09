<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Logged In" />
<%@ include file="_header.jspf" %>

<div class="form-card text-center">
    <h1 class="page-title">You're logged in</h1>
    <p class="page-subtitle">Welcome back, <c:out value="${loginUser.name}" />!</p>
    <p class="mb-3">Role: <span class="badge badge-${loginUser.role}">${loginUser.role}</span></p>
    <a href="${ctx}/${dashboardLink}" class="btn btn-primary btn-block">Go to my dashboard</a>
    <a href="${ctx}/products" class="btn btn-outline btn-block mt-1">Continue shopping</a>
</div>

<%@ include file="_footer.jspf" %>
