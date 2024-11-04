plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.report"))
    api(libs.org.ow2.asm.asm.analysis)
    api(libs.org.ow2.asm.asm.util)
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Core"

java {
    withJavadocJar()
}
