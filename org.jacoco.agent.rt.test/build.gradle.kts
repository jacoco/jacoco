plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.agent.rt"))
    api(project(":org.jacoco.core.test"))
    api(libs.junit.junit)
}

description = "JaCoCo :: Test :: Agent RT"

java {
    withJavadocJar()
}
