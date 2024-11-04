plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.agent"))
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Agent"

java {
    withJavadocJar()
}
