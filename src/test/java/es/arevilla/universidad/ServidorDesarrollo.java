package es.arevilla.universidad;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.nio.file.Files;

/**
 * Tomcat embebido con la aplicación tal cual está en src/main/webapp y
 * target/classes. Lo usan los tests de integración y sirve para desarrollar
 * sin instalar Tomcat:
 *
 *   ./mvnw compile exec:java@dev      ->  http://localhost:8080
 */
public final class ServidorDesarrollo {

    private ServidorDesarrollo() {
    }

    public static Tomcat arrancar(int puerto) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("tomcat-universidad").toString());
        tomcat.setPort(puerto);
        tomcat.getConnector();

        Context contexto = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot recursos = new StandardRoot(contexto);
        recursos.addPreResources(new DirResourceSet(recursos, "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        contexto.setResources(recursos);

        tomcat.start();
        return tomcat;
    }

    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Tomcat tomcat = arrancar(puerto);
        System.out.println("Aplicación en http://localhost:" + puerto + "  (usuario admin, contraseña admin)");
        tomcat.getServer().await();
    }
}
