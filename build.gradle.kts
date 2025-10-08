plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
    id("maven-publish")
}

group = "com.github.groundbreakingmc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // UTILS

    // https://github.com/groundbreakingmc/Fission
    compileOnly("com.github.groundbreakingmc:Fission:1.0.0")

    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    compileOnly("it.unimi.dsi:fastutil:8.5.16")

    // https://github.com/JetBrains/java-annotations
    compileOnly("org.jetbrains:annotations:26.0.2")

    // TESTS
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("com.github.groundbreakingmc:Fission:1.0.0")

    testImplementation("it.unimi.dsi:fastutil:8.5.16")

    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    jmhImplementation("org.openjdk.jmh:jmh-core:1.37")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-generator-annprocess
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // OTHER TOML PARSE LIBS
    // need for jmh test (not recommended to use)

    //https://github.com/mwanji/toml4j
    jmhImplementation("com.moandjiezana.toml:toml4j:0.7.2")
    // https://github.com/tomlj/tomlj
    jmhImplementation("org.tomlj:tomlj:1.1.1")
}

tasks.register<JavaExec>("jmhWarmup") {
    group = "benchmark"
    description = "Run JMH benchmarks with warmup"
    classpath = tasks.jmhJar.get().outputs.files + configurations.jmh.get()
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(
            "-bm", "AverageTime", // BenchmarkMode
            "-tu", "ns",          // TimeUnit: ns
            "-wi", "3",           // Warmup iterations
            "-i", "5",            // Measurement iteration
            "-t", "5",            // Measurement time
            "-f", "5",            // Forks
    )
}

tasks.register<JavaExec>("jmhCold") {
    group = "benchmark"
    description = "Run JMH benchmarks without warmup (cold state)"
    classpath = tasks.jmhJar.get().outputs.files + configurations.jmh.get()
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(
            "-bm", "SingleShotTime", // BenchmarkMode
            "-tu", "ns", // TimeUnit: ns
            "-wi", "0",  // Warmup iterations
            "-i", "1",   // Measurement iteration
            "-f", "1",   // Forks
    )
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name = "TOMLy"
                description = "A high-performance, fully compliant TOML v1.0.0 parser for Java with comment preservation and flexible configuration options."
                url = "https://github.com/groundbreakingmc/TOMLy"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://github.com/groundbreakingmc/TOMLy?tab=Apache-2.0-1-ov-file"
                    }
                }

                developers {
                    developer {
                        id = "GroundbreakingMC"
                        name = "Victor"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/groundbreakingmc/TOMLy.git"
                    developerConnection = "scm:git:ssh://git@github.com:groundbreakingmc/TOMLy.git"
                    url = "https://github.com/groundbreakingmc/TOMLy"
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.release = 17
    }
    withType<Jar> {
        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = project.version
        }
    }
    withType<Javadoc> {
        options {
            encoding = "UTF-8"
            (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }
}
