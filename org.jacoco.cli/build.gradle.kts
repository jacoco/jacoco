plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core"))
    api(project(":org.jacoco.report"))
    api(libs.args4j.args4j)
}

description = "JaCoCo :: Command Line Interface"

java {
    withJavadocJar()
}
