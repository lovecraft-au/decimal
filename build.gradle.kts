import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.palantirGitVersion)
    alias(libs.plugins.maven.central.publish)
    id("maven-publish")
}

group = "au.lovecraft"

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion().removePrefix("v")

kotlin {
    jvmToolchain(21)
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-XXLanguage:+CustomEqualsInValueClasses"
        )
    }
    androidLibrary {
        namespace = "com.capy.budget.client.shared"
        compileSdk = 36
        minSdk = 29
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val jvmCommonMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.configure {
            dependsOn(jvmCommonMain)
        }
        jvmMain.configure {
            dependsOn(jvmCommonMain)
        }
        iosMain.configure {
            dependsOn(commonMain.get())
        }
        iosArm64Main {
            dependsOn(iosMain.get())
        }
        iosSimulatorArm64Main {
            dependsOn(iosMain.get())
        }
        wasmJsMain.dependencies {
            implementation(
                dependencyNotation = npm(
                    name = "decimal.js",
                    version = libs.versions.decimal.js.get()
                )
            )
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), name, version.toString())

    pom {
        name.set("Decimal")
        description.set("A description of what my library does.")
        inceptionYear.set("2025")
        url.set("https://github.com/lovecraft-au/decimal")
        licenses {
            license {
                name.set("GNU Lesser General Public License")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                distribution.set("https://www.gnu.org/licenses/lgpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("chris-hatton")
                name.set("Christopher Hatton")
                url.set("https://github.com/chris-hatton")
            }
        }
        scm {
            url.set("https://github.com/lovecraft-au/decimal")
            connection.set("scm:git:git@github.com:lovecraft-au/decimal.git")
            developerConnection.set("scm:git:ssh://git@github.com:lovecraft-au/decimal.git")
        }
    }
}
