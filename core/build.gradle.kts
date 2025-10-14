plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:3.5.6"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    runtimeOnly("io.asyncer:r2dbc-mysql:1.4.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}