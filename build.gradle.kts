plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("checkProjectHealth") {
    group = "verification"
    description = "Cleans, Builds, and Runs all Unit Tests to ensure the project is stable."

    dependsOn("clean", ":app:assembleDebug", ":app:testDebugUnitTest")

    doLast {
        println("\n--------------------------------------------------")
        println("✅ PROJECT HEALTH CHECK PASSED!")
        println("--------------------------------------------------\n")
    }
}

