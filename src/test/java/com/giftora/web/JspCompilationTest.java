package com.giftora.web;

import org.apache.jasper.JspC;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Precompiles every JSP/JSPF under src/main/webapp using Tomcat 9's Jasper compiler.
 * This catches taglib, EL and scriptlet mistakes at build time instead of at runtime.
 */
class JspCompilationTest {

    @Test
    void allJspsCompile() throws Exception {
        Path outputDir = Files.createTempDirectory("giftora-jspc");

        JspC jspc = new JspC();
        jspc.setUriroot(new File("src/main/webapp").getAbsolutePath());
        jspc.setOutputDir(outputDir.toAbsolutePath().toString());
        jspc.setFailOnError(true);
        jspc.setCompile(true);
        jspc.execute();

        long generated = Files.walk(outputDir)
                .filter(p -> p.toString().endsWith(".class"))
                .count();
        assertTrue(generated > 0, "Jasper should compile the JSPs to Java classes");
    }
}
