plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core"))
    api(project(":org.jacoco.report"))
    api(project(":org.jacoco.agent"))
    api(project(":org.jacoco.agent.rt"))
    api(project(":org.jacoco.ant"))
    api(project(":org.jacoco.cli"))
    api(project(":org.jacoco.examples"))
    api(project(":jacoco-maven-plugin"))
    testImplementation(project(":org.jacoco.core.test"))
    testImplementation(project(":org.jacoco.report.test"))
    testImplementation(project(":org.jacoco.agent.test"))
    testImplementation(project(":org.jacoco.agent.rt.test"))
    testImplementation(project(":org.jacoco.ant.test"))
    testImplementation(project(":org.jacoco.cli.test"))
    testImplementation(project(":org.jacoco.examples.test"))
    testImplementation(project(":jacoco-maven-plugin.test"))
}

description = "JaCoCo :: Documentation"

java {
    withJavadocJar()
}
