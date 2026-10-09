<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Logged Out" />
<%@ include file="_header.jspf" %>

<div class="form-card text-center">
    <h1 class="page-title">You've been logged out</h1>
    <p class="page-subtitle">Thank you for visiting Giftora. See you again soon!</p>
    <a href="${ctx}/login" class="btn btn-primary btn-block">Log in again</a>
    <a href="${ctx}/products" class="btn btn-outline btn-block mt-1">Browse products</a>
</div>

<%@ include file="_footer.jspf" %>
