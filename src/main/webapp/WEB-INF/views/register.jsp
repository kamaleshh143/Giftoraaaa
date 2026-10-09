<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Create Account" />
<%@ include file="_header.jspf" %>

<div class="form-card">
    <h1 class="page-title text-center">Create your account</h1>
    <p class="page-subtitle text-center">Join Giftora to shop or sell.</p>

    <form method="post" action="${ctx}/register">
        <div class="form-group">
            <label for="name">Full name</label>
            <input class="form-control" type="text" id="name" name="name" value="<c:out value='${name}'/>" required minlength="2" />
        </div>
        <div class="form-group">
            <label for="email">Email address</label>
            <input class="form-control" type="email" id="email" name="email" value="<c:out value='${email}'/>" required />
        </div>
        <div class="form-row">
            <div class="form-group">
                <label for="password">Password</label>
                <input class="form-control" type="password" id="password" name="password" required minlength="8" />
                <p class="form-hint">At least 8 characters.</p>
            </div>
            <div class="form-group">
                <label for="confirmPassword">Confirm password</label>
                <input class="form-control" type="password" id="confirmPassword" name="confirmPassword" required minlength="8" />
            </div>
        </div>
        <div class="form-group">
            <label for="role">I want to</label>
            <select class="form-control" id="role" name="role">
                <option value="BUYER" ${role == 'SELLER' ? '' : 'selected'}>Shop as a buyer</option>
                <option value="SELLER" ${role == 'SELLER' ? 'selected' : ''}>Sell as a seller</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary btn-block">Create account</button>
    </form>
    <p class="text-center mt-2 text-muted">Already registered? <a href="${ctx}/login">Log in</a></p>
</div>

<%@ include file="_footer.jspf" %>
