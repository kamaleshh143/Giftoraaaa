<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Manage Users" />
<%@ include file="/WEB-INF/views/_header.jspf" %>

<h1 class="page-title mb-2">Users</h1>

<div class="table-wrap">
    <table class="table">
        <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Status</th><th>Action</th></tr></thead>
        <tbody>
        <c:forEach var="u" items="${users}">
            <tr>
                <td><c:out value="${u.name}" /></td>
                <td><c:out value="${u.email}" /></td>
                <td><span class="badge">${u.role}</span></td>
                <td><fmt:formatDate value="${u.createdAtDate}" pattern="dd MMM yyyy" /></td>
                <td><span class="badge ${u.active ? 'badge-active' : 'badge-inactive'}">${u.active ? 'Active' : 'Disabled'}</span></td>
                <td>
                    <form method="post" action="${ctx}/admin/users" class="inline-form">
                        <input type="hidden" name="userId" value="${u.id}" />
                        <input type="hidden" name="active" value="${not u.active}" />
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <button class="btn ${u.active ? 'btn-danger' : 'btn-primary'} btn-sm" type="submit">${u.active ? 'Disable' : 'Enable'}</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>

