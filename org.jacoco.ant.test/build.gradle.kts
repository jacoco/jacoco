plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.ant"))
    api(libs.junit.junit)
    api(libs.org.apache.ant.ant.antunit)
    api(libs.org.apache.ant.ant.launcher)
    api(libs.org.apache.ant.ant.junit)
    api(libs.org.apache.ant.ant.junit4)
}

description = "JaCoCo :: Test :: Ant"

java {
    withJavadocJar()
}
