plugins {
    kotlin("jvm") version "1.7.0"
    application
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib"))
}
application {
    mainClass.set("MainKt")
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "MainKt"
        }
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}