<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giftora - Welcome</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header class="header">
        <div class="container nav">
            <div class="logo">Giftora</div>
            <nav class="nav-links">
                <a href="${pageContext.request.contextPath}/products">Products</a>
                <a href="${pageContext.request.contextPath}/login">Login</a>
                <a href="${pageContext.request.contextPath}/register">Register</a>
                <a href="${pageContext.request.contextPath}/cart">Cart</a>
            </nav>
        </div>
    </header>
    <main class="main">
        <div class="container">
            <section class="hero">
                <h1>Welcome to Giftora</h1>
                <p>Find the perfect gifts for every occasion</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Shop Now</a>
            </section>
        </div>
    </main>
    <footer class="footer">
        <div class="container">
            <p>&copy; 2026 Giftora. All rights reserved.</p>
        </div>
    </footer>
</body>
</html>
