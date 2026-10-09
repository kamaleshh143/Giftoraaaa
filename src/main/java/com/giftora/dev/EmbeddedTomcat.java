package com.giftora.dev;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Runs the Giftora web application on an embedded Apache Tomcat 9 instance
 * (the same Servlet 4 / {@code javax.servlet} stack used in production) so the
 * site can be opened and used in a browser from VS Code. No separately installed
 * Tomcat is required.
 *
 * <p>Start it from the project directory (the folder containing {@code pom.xml}):
 * <pre>
 *   mvn -Pdev test-compile exec:exec
 * </pre>
 * then open <a href="http://localhost:8080/">http://localhost:8080/</a>.
 *
 * <p>The H2 database is written to {@code ./data} so registered users and orders
 * survive restarts. Set {@code -Dgiftora.port=9090} (or the {@code PORT}
 * environment variable) to change the port.
 */
public final class EmbeddedTomcat {

    private EmbeddedTomcat() {
    }

    public static void main(String[] args) throws Exception {
        Path projectDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path webappDir = projectDir.resolve("src").resolve("main").resolve("webapp");
        Path classesDir = projectDir.resolve("target").resolve("classes");

        require(webappDir, "web application", "Run this from the project root (the folder containing pom.xml).");
        require(classesDir, "compiled classes (target/classes)", "Run 'mvn test-compile' first.");

        // Local, persistent, git-ignored database directory.
        Path dataDir = projectDir.resolve("data");
        Files.createDirectories(dataDir);
        System.setProperty("GIFTORA_DB_DIR", dataDir.toString());
        System.setProperty("GIFTORA_SEED", System.getProperty("GIFTORA_SEED", "true"));
        System.setProperty("AI_CHATBOT_PROVIDER", System.getProperty("AI_CHATBOT_PROVIDER", "mock"));

        int port = resolvePort();

        Path baseDir = projectDir.resolve("target").resolve("tomcat-dev");
        Files.createDirectories(baseDir);

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context context = tomcat.addWebapp("", webappDir.toString());

        // Serve freshly compiled classes as /WEB-INF/classes, so edits picked up by
        // 'mvn test-compile' are used without any copy step.
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                classesDir.toString(), "/"));
        context.setResources(resources);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (Exception ignored) {
                // JVM is shutting down anyway
            }
        }, "giftora-shutdown"));

        try {
            tomcat.start();
        } catch (LifecycleException ex) {
            System.err.println();
            System.err.println("Failed to start Giftora on port " + port + ".");
            System.err.println("Is another instance already running? Try -Dgiftora.port=9090.");
            throw ex;
        }

        System.out.println();
        System.out.println("========================================================");
        System.out.println("  Giftora is running at: http://localhost:" + port + "/");
        System.out.println("  Database directory   : " + dataDir);
        System.out.println("  Press Ctrl+C in this terminal to stop.");
        System.out.println("========================================================");
        System.out.println();

        tomcat.getServer().await();
    }

    private static void require(Path path, String what, String hint) {
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("Cannot find " + what + ": " + path + "  (" + hint + ")");
        }
    }

    private static int resolvePort() {
        String property = System.getProperty("giftora.port");
        if (property != null && !property.isBlank()) {
            return Integer.parseInt(property.trim());
        }
        String env = System.getenv("PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        return 8080;
    }
}
