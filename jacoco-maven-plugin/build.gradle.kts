plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(libs.org.codehaus.plexus.plexus.utils)
    api(libs.org.apache.maven.shared.file.management)
    api(libs.org.apache.maven.reporting.maven.reporting.api)
    api(project(":org.jacoco.agent"))
    api(project(":org.jacoco.core"))
    api(project(":org.jacoco.report"))
    compileOnly(libs.org.apache.maven.maven.plugin.api)
    compileOnly(libs.org.apache.maven.maven.core)
    compileOnly(libs.org.apache.maven.plugin.tools.maven.plugin.annotations)
}

description = "JaCoCo :: Maven Plugin"

java {
    withJavadocJar()
}
