plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(libs.org.ow2.asm.asm)
    api(libs.org.ow2.asm.asm.commons)
    api(libs.org.ow2.asm.asm.tree)
}

description = "JaCoCo :: Core"

java {
    withJavadocJar()
}
