plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.wzpmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.0")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.0")
    compileOnly(gradleApi())
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    manifest {
        attributes["Premain-Class"] = "cn.wzpmc.Agent"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.test {
    useJUnitPlatform()
}