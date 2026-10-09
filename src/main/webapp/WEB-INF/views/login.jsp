<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Log In" />
<%@ include file="_header.jspf" %>

<div class="form-card">
    <h1 class="page-title text-center">Welcome back</h1>
    <p class="page-subtitle text-center">Log in to continue shopping.</p>
    <c:if test="${not empty param.error}">
        <div class="alert alert-error">Invalid email or password.</div>
    </c:if>
    <form method="post" action="${ctx}/login">
        <div class="form-group">
            <label for="email">Email address</label>
            <input class="form-control" type="email" id="email" name="email" value="<c:out value='${email}'/>" required />
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input class="form-control" type="password" id="password" name="password" required />
        </div>
        <button type="submit" class="btn btn-primary btn-block">Log in</button>
    </form>
    <p class="text-center mt-2 text-muted">New to Giftora? <a href="${ctx}/register">Create an account</a></p>
</div>

<%@ include file="_footer.jspf" %>
