plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.report"))
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Report"

java {
    withJavadocJar()
}
