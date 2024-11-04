plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.report"))
}

description = "JaCoCo :: Examples"

java {
    withJavadocJar()
}
