package com.semanticsoft.patientmobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.semanticsoft.patientmobile.ui.navigation.AppDestination
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardEvent
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardViewModel
import com.semanticsoft.patientmobile.ui.screens.login.LoginEvent
import com.semanticsoft.patientmobile.ui.screens.login.LoginScreen
import com.semanticsoft.patientmobile.ui.screens.login.LoginViewModel
import com.semanticsoft.patientmobile.ui.screens.navigation.NavScreen
import com.semanticsoft.patientmobile.ui.screens.registration.RegistrationEvent
import com.semanticsoft.patientmobile.ui.screens.registration.RegistrationScreen
import com.semanticsoft.patientmobile.ui.screens.registration.RegistrationViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatientMobileApp(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            val vm: LoginViewModel = hiltViewModel()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is LoginEvent.LoginSuccess -> {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Login.route) { inclusive = true }
                            }
                        }

                        is LoginEvent.LoginFailure -> Unit
                    }
                }
            }

            LoginScreen(
                state = vm.state,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onLoginClick = vm::login,
                onGoToRegister = { navController.navigate(AppDestination.Registration.route) }
            )
        }

        composable(AppDestination.Registration.route) {
            val vm: RegistrationViewModel = hiltViewModel()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is RegistrationEvent.RegistrationSuccess -> {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Login.route)
                            }
                        }

                        is RegistrationEvent.RegistrationFailure -> Unit
                    }
                }
            }

            RegistrationScreen(
                state = vm.state,
                onRoleChange = vm::onRoleChange,
                onNameChange = vm::onNameChange,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onRegisterClick = vm::register,
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Dashboard.route) {
            val vm: DashboardViewModel = hiltViewModel()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        DashboardEvent.LogoutSuccess -> {
                            navController.navigate(AppDestination.Login.route) {
                                popUpTo(AppDestination.Dashboard.route) { inclusive = true }
                            }
                        }

                        is DashboardEvent.LogoutFailure -> Unit
                        DashboardEvent.RefreshCompleted -> Unit
                        is DashboardEvent.RefreshFailed -> Unit
                    }
                }
            }

            NavScreen(
                state = vm.state,
                onRefresh = vm::refresh,
                onLogout = vm::logout
            )
        }
    }
}
