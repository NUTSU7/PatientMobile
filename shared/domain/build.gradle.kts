plugins {
    id("patient.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:fhir"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
