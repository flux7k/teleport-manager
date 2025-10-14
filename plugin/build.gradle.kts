plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":core"))

    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework:spring-tx")
}

tasks.shadowJar {
    mergeServiceFiles {
        path = "META-INF/spring"
    }
    archiveBaseName.set(rootProject.name)
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}