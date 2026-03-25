package com.semanticsoft.patientmobile

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.semanticsoft.patientmobile.ui.navigation.AppDestination
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardViewModel
import com.semanticsoft.patientmobile.ui.screens.login.LoginScreen
import com.semanticsoft.patientmobile.ui.screens.login.LoginViewModel
import com.semanticsoft.patientmobile.ui.screens.registration.RegistrationScreen
import com.semanticsoft.patientmobile.ui.screens.registration.RegistrationViewModel
import com.semanticsoft.patientmobile.ui.screens.upload.UploadScreen
import com.semanticsoft.patientmobile.ui.screens.upload.UploadViewModel

@Composable
fun PatientMobileApp(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            val vm: LoginViewModel = viewModel()
            LoginScreen(
                state = vm.state,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onLoginClick = {
                    vm.login()
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(AppDestination.Registration.route) }
            )
        }

        composable(AppDestination.Registration.route) {
            val vm: RegistrationViewModel = viewModel()
            RegistrationScreen(
                state = vm.state,
                onRoleChange = vm::onRoleChange,
                onNameChange = vm::onNameChange,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onRegisterClick = {
                    vm.register()
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Login.route)
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Dashboard.route) {
            val vm: DashboardViewModel = viewModel()
            DashboardScreen(
                state = vm.state,
                onUploadClick = { navController.navigate(AppDestination.Upload.route) }
            )
        }

        composable(AppDestination.Upload.route) {
            val vm: UploadViewModel = viewModel()
            UploadScreen(
                state = vm.state,
                onDocumentNameChange = vm::onDocumentNameChange,
                onNotesChange = vm::onNotesChange,
                onUploadClick = vm::upload,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
