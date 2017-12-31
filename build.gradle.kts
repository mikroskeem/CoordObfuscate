plugins {
    kotlin("jvm") version "1.2.10"
    id("net.minecrell.licenser") version "0.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.2.1"
}

configurations {
    create("shadow")

    getByName("compileOnly").extendsFrom(getByName("shadow"))
}

val gradleWrapperVersion: String by extra
val kotlinVersion: String by extra
val paperApiVersion: String by extra
val protocollibApiVersion: String by extra

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        name = "dmulloy-repo"
        setUrl("http://repo.dmulloy2.net/nexus/repository/public/")
    }

    maven {
        name = "destroystokyo-repo"
        setUrl("https://repo.destroystokyo.com/repository/maven-public/")
    }
}

dependencies {
    add("shadow", kotlin("stdlib-jdk8", kotlinVersion))
    compileOnly("com.destroystokyo.paper:paper-api:$paperApiVersion") {
        exclude(module = "*")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib-API:$protocollibApiVersion") {
        exclude(module = "*")
    }
}

license {
    header = rootProject.file("etc/HEADER")
    filter.include("**/*.kt")
}

bukkit {
    name = "CoordObfuscate"
    main = "eu.mikroskeem.coordobfuscate.CoordObfuscate"
    description = "Obfuscates coordinates by adding random offset to them"
    website = "https://mikroskeem.eu"
    authors = listOf("mikroskeem")
    depend = listOf("ProtocolLib")

    permissions {
        "coordobfuscate.ignore" {
            description = "Players with given permission node won't get random coordinate offset applied"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.FALSE
        }
    }
}

tasks.getting(Jar::class) {
    from(configurations["shadow"].map<File, Any> { if(it.isDirectory) it else zipTree(it) })
}

tasks.getting(Wrapper::class) {
    gradleVersion = gradleWrapperVersion
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}

defaultTasks("licenseFormat", "build")