plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.cli"))
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Command Line Interface"

java {
    withJavadocJar()
}
