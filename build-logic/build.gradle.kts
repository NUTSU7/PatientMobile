plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)

    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        register("patientKmpLibrary") {
            id = "patient.kmp.library"
            implementationClass = "com.estcomputer.patient.buildlogic.PatientMultiplatformLibraryPlugin"
        }
        register("patientAndroidJvmLibrary") {
            id = "patient.android.jvm.library"
            implementationClass = "com.estcomputer.patient.buildlogic.PatientAndroidJvmLibraryPlugin"
        }
        register("patientVerification") {
            id = "patient.verification"
            implementationClass = "com.estcomputer.patient.buildlogic.PatientVerificationPlugin"
        }
    }
}
