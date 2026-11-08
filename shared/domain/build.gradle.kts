plugins {
    id("patient.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:fhir"))
        }
    }
}
