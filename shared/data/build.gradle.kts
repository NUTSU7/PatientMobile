plugins {
    id("patient.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(project(":shared:fhir"))
        }
    }
}
