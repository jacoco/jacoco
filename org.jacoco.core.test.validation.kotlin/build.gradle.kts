plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":org.jacoco.core.test"))
    api(libs.org.jetbrains.kotlin.kotlin.stdlib)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
}

description = "JaCoCo :: Test :: Core :: Validation Kotlin"

java {
    withJavadocJar()
}
