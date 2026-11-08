plugins {
    id("patient.android.jvm.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
        }
    }
}
