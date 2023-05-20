plugins {
  id("java")
  id("groovy")
  id("org.jetbrains.kotlin.jvm") version "1.8.20"
  id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.github.lppedd"
version = "0.1.0"

repositories {
  mavenCentral()
}

intellij {
  version.set("231.8109.175")
  type.set("IC")
  downloadSources.set(true)
  pluginName.set("idea-jenkins-pipeline")
  plugins.set(listOf("java", "Groovy" /* Direct dependencies */))
}

dependencies {
  // Must be the same as the one used by the IntelliJ development instance
  compileOnly("org.codehaus.groovy:groovy-all:3.0.13")
}

tasks {
  wrapper {
    distributionType = Wrapper.DistributionType.ALL
  }

  compileGroovy {
    destinationDirectory.set(file("build/resources/main/groovy/classes"))
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  compileKotlin {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set("231")
    untilBuild.set("232.*")
  }

  runPluginVerifier {
    ideVersions.set(listOf(
        "IC-2023.1",
    ))
  }
}
