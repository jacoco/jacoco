plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core"))
    api(project(":org.jacoco.report"))
    api(project(":org.jacoco.agent"))
    compileOnly(libs.org.apache.ant.ant)
}

description = "JaCoCo :: Ant"

java {
    withJavadocJar()
}
