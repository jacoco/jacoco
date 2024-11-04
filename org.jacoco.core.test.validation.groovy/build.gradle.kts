plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core.test"))
    api(libs.org.codehaus.groovy.groovy)
}

description = "JaCoCo :: Test :: Core :: Validation Groovy"

java {
    withJavadocJar()
}
