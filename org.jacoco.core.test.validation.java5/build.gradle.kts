plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core.test"))
}

description = "JaCoCo :: Test :: Core :: Validation Java 5"

java {
    withJavadocJar()
}
