plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core.test"))
    api(libs.org.scala.lang.scala.library)
}

description = "JaCoCo :: Test :: Core :: Validation Scala"

java {
    withJavadocJar()
}
