package com.giftora.web;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots the real application on an embedded Tomcat 9 instance (same Servlet 4 / javax
 * stack used in production) and exercises the HTTP layer end to end: filter chain,
 * servlets, H2 database initialisation, seeded data, JSON API and JSP rendering.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationSmokeTest {

    private Tomcat tomcat;
    private HttpClient http;
    private String baseUrl;

    @BeforeAll
    void startServer() throws Exception {
        Path webapp = Files.createTempDirectory("giftora-webapp");
        copyTree(Paths.get("src", "main", "webapp"), webapp);
        copyTree(Paths.get("target", "classes"), webapp.resolve("WEB-INF").resolve("classes"));

        Path baseDir = Files.createTempDirectory("giftora-tomcat");
        Path dbDir = Files.createTempDirectory("giftora-db");
        System.setProperty("GIFTORA_DB_DIR", dbDir.toString());
        System.setProperty("GIFTORA_SEED", "true");
        System.setProperty("AI_CHATBOT_PROVIDER", "mock");

        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(0);
        tomcat.getConnector();

        Context context = tomcat.addWebapp("", webapp.toAbsolutePath().toString());

        tomcat.start();

        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort();
        http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @AfterAll
    void stopServer() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        HttpResponse<String> response = get("/api/v1/health");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"), "Health payload should report UP: " + response.body());
    }

    @Test
    void homePageRenders() throws Exception {
        HttpResponse<String> response = get("/");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Giftora"));
    }

    @Test
    void productCatalogueListsSeededProducts() throws Exception {
        HttpResponse<String> response = get("/products");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("50 products available"),
                "Catalogue should report the seeded catalogue size");
        assertTrue(response.body().contains("card-title"), "Catalogue should render product cards");
    }

    @Test
    void productApiReturnsJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/products");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(response.body().contains("Aurora X5 Smartphone"));
    }

    @Test
    void productDetailsPageRenders() throws Exception {
        HttpResponse<String> response = get("/product?id=1");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Aurora X5 Smartphone"));
    }

    @Test
    void demoBuyerCanLogIn() throws Exception {
        String form = "email=" + enc("buyer@giftora.example") + "&password=" + enc("Buyer@12345");
        HttpResponse<String> response = post("/login", form, null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Giftora Buyer"), "Login should greet the buyer: " + response.body());
    }

    @Test
    void protectedPageRedirectsAnonymousUserToLogin() throws Exception {
        HttpResponse<String> response = get("/orders");
        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("Location").orElse("").contains("/login"),
                "Anonymous access to /orders should redirect to the login page");
    }

    @Test
    void buyerCanCompleteAPurchaseFlow() throws Exception {
        String cookie = login("buyer@giftora.example", "Buyer@12345");
        assertTrue(cookie.contains("JSESSIONID"), "Login should establish a session");

        // CSRF token is bound to the session and rendered into forms
        HttpResponse<String> productPage = getWithCookie("/product?id=1", cookie);
        String csrf = extract(productPage.body(), "name=\"csrfToken\" value=\"([^\"]*)\"");
        assertTrue(csrf != null && !csrf.isBlank(), "Product page should expose a CSRF token");

        HttpResponse<String> addToCart = postWithCookie("/cart",
                "action=add&productId=1&quantity=2&csrfToken=" + enc(csrf), cookie);
        assertEquals(302, addToCart.statusCode());

        HttpResponse<String> checkoutPage = getWithCookie("/checkout", cookie);
        assertEquals(200, checkoutPage.statusCode());
        String checkoutToken = extract(checkoutPage.body(), "name=\"checkoutToken\" value=\"([^\"]*)\"");
        String checkoutCsrf = extract(checkoutPage.body(), "name=\"csrfToken\" value=\"([^\"]*)\"");
        assertTrue(checkoutToken != null && !checkoutToken.isBlank(), "Checkout should issue an idempotency token");

        String body = "shippingName=" + enc("Giftora Buyer")
                + "&shippingAddress=" + enc("1 Gift Street, Testville")
                + "&shippingPhone=" + enc("0100000000")
                + "&checkoutToken=" + enc(checkoutToken)
                + "&csrfToken=" + enc(checkoutCsrf);
        HttpResponse<String> placeOrder = postWithCookie("/checkout", body, cookie);
        assertEquals(302, placeOrder.statusCode());
        String location = placeOrder.headers().firstValue("Location").orElse("");
        assertTrue(location.contains("/order-success?order="), "Checkout should redirect to the success page: " + location);

        HttpResponse<String> success = getWithCookie(location.substring(location.indexOf("/order-success")), cookie);
        assertEquals(200, success.statusCode());
        assertTrue(success.body().contains("Thank you for your order"),
                "Order confirmation page should render");

        HttpResponse<String> history = getWithCookie("/orders", cookie);
        assertEquals(200, history.statusCode());
        assertTrue(history.body().contains("My Orders"), "Order history should render");

        String trackLink = extract(history.body(), "(/order/track\\?id=\\d+)");
        assertTrue(trackLink != null, "Order history should link to order tracking");
        HttpResponse<String> tracking = getWithCookie(trackLink, cookie);
        assertEquals(200, tracking.statusCode());
        assertTrue(tracking.body().contains("PENDING"), "Tracking page should render the order status");
    }

    @Test
    void newBuyerCanRegister() throws Exception {
        String form = "name=" + enc("Test Newcomer")
                + "&email=" + enc("newcomer@example.com")
                + "&password=" + enc("Password1")
                + "&confirmPassword=" + enc("Password1")
                + "&role=BUYER";
        HttpResponse<String> response = post("/register", form, null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Newcomer"), "Registration should confirm the new account");
    }

    @Test
    void sellerDashboardRenders() throws Exception {
        String cookie = login("seller@giftora.example", "Seller@12345");
        assertTrue(getWithCookie("/seller/dashboard", cookie).body().contains("Seller dashboard"));
        assertEquals(200, getWithCookie("/seller/products", cookie).statusCode());
        assertEquals(200, getWithCookie("/seller/orders", cookie).statusCode());
    }

    @Test
    void adminDashboardAndSectionsRender() throws Exception {
        String cookie = login("admin@giftora.example", "Admin@12345");
        assertTrue(getWithCookie("/admin/dashboard", cookie).body().contains("Admin dashboard"));
        assertEquals(200, getWithCookie("/admin/users", cookie).statusCode());
        assertEquals(200, getWithCookie("/admin/products", cookie).statusCode());
        assertEquals(200, getWithCookie("/admin/orders", cookie).statusCode());
        assertEquals(200, getWithCookie("/admin/reviews", cookie).statusCode());
    }

    @Test
    void chatbotApiReturnsARecomendation() throws Exception {
        HttpResponse<String> response = postJson("/api/v1/chatbot", "{\"message\":\"gift under 1000\"}");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("reply"), "Chatbot should return a reply: " + response.body());
    }

    private String login(String email, String password) throws Exception {
        String form = "email=" + enc(email) + "&password=" + enc(password);
        HttpResponse<String> response = post("/login", form, null);
        assertEquals(200, response.statusCode());
        return response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("JSESSIONID="))
                .findFirst()
                .map(c -> c.substring(0, c.indexOf(';')))
                .orElse("");
    }

    private String extract(String body, String regex) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private HttpResponse<String> getWithCookie(String path, String cookie) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Cookie", cookie)
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postWithCookie(String path, String body, String cookie)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Cookie", cookie)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body, String cookie) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (cookie != null) {
            builder.header("Cookie", cookie);
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path destination = target.resolve(source.relativize(path).toString());
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.createDirectories(destination.getParent());
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
}
