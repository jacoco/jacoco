plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core"))
}

description = "JaCoCo :: Report"

java {
    withJavadocJar()
}
