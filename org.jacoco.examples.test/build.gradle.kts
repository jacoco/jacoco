plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.examples"))
    api(project(":org.jacoco.agent.rt"))
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Examples"

java {
    withJavadocJar()
}
