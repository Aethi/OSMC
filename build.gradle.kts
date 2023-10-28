plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application.mainClass = "com.osmc.core.MinecraftCore"

group = "org.osmc"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation(files("lib/bukkit.jar"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of java you want to use,
    // the minimum required for JDA is 1.8
    sourceCompatibility = "1.8"
}