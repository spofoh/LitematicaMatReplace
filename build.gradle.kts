plugins {
    id("dev.kikugie.loom-back-compat")
}

group = property("maven_group") as String
version = property("mod_version") as String
base.archivesName = "${property("mod_file_name")}-${sc.current.version}"

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository {
            maven(url) { name = alias }
        }
        filter {
            groups.forEach(::includeGroupAndSubgroups)
        }
    }

    strictMaven(
        "https://masa.dy.fi/maven/sakura-ryoko",
        "MasaModding",
        "fi.dy.masa"
    )
    strictMaven(
        "https://maven.fallenbreath.me/releases",
        "FallenBreath",
        "me.fallenbreath"
    )
    strictMaven(
        "https://api.modrinth.com/maven",
        "Modrinth",
        "maven.modrinth"
    )
    maven("https://jitpack.io") {
        name = "JitPack"
        content {
            includeGroupAndSubgroups("com.github")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")

    // Hard runtime dependencies. Loom exposes them to compilation and the dev
    // client but does not bundle or shade them into LMR's distributable jar.
    modImplementation(
        "fi.dy.masa.malilib:malilib-fabric-${property("dependency_minecraft_version")}:${property("malilib_version")}"
    )
    modImplementation("maven.modrinth:bEpr0Arc:${property("litematica_modrinth_id")}")

    testImplementation(
        "net.fabricmc:fabric-loader-junit:${property("fabric_loader_version")}"
    )
}

loom {
    runConfigs.all {
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Xmx2G")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

val modJsonProperties = mapOf(
    "mod_version" to property("mod_version") as String,
    "minecraft_compat" to property("minecraft_compat") as String,
    "malilib_compat" to property("malilib_compat") as String,
    "litematica_compat" to property("litematica_compat") as String,
    "fabric_loader_version" to property("fabric_loader_version") as String
)

tasks.processResources {
    modJsonProperties.forEach(inputs::property)
    filesMatching("fabric.mod.json") {
        expand(modJsonProperties)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.matching {
    name.contains("sourcesJar", ignoreCase = true)
}.configureEach {
    enabled = false
}
