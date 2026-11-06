package com.estcomputer.patient.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.estcomputer.patient.feature.auth.AuthEvent
import com.estcomputer.patient.feature.auth.AuthState
import com.estcomputer.patient.feature.auth.AuthViewModel
import com.estcomputer.patient.feature.auth.login.LoginEvent
import com.estcomputer.patient.feature.auth.login.LoginScreen
import com.estcomputer.patient.feature.auth.login.LoginViewModel
import com.estcomputer.patient.feature.patient.analysisExplanation.AnalysisExplanationScreen
import com.estcomputer.patient.feature.patient.analysisExplanation.AnalysisExplanationViewModel
import com.estcomputer.patient.feature.patient.dashboard.DashboardEvent
import com.estcomputer.patient.feature.patient.dashboard.DashboardViewModel
import com.estcomputer.patient.feature.patient.navigation.PatientNavScreen
import com.estcomputer.patient.feature.patient.registration.RegistrationEvent
import com.estcomputer.patient.feature.patient.registration.RegistrationScreen
import com.estcomputer.patient.feature.patient.registration.RegistrationViewModel
import com.estcomputer.patient.feature.patient.reportResults.ReportResultsScreen
import com.estcomputer.patient.feature.patient.reportResults.ReportResultsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RootNavHost(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(authViewModel) {
        authViewModel.events.collectLatest { event ->
            when (event) {
                AuthEvent.NavigateToLogin -> {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.SessionExpired) {
            navController.navigate(AppDestination.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            val vm: LoginViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is LoginEvent.LoginSuccess -> {
                            navController.navigate(AppDestination.Patient.route) {
                                popUpTo(AppDestination.Login.route) { inclusive = true }
                            }
                        }
                        is LoginEvent.LoginFailure -> Unit
                    }
                }
            }

            LoginScreen(
                state = state,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onLoginClick = vm::login,
                onGoToRegister = { navController.navigate(AppDestination.PatientRegistration.route) }
            )
        }

        composable(AppDestination.PatientRegistration.route) {
            val vm: RegistrationViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is RegistrationEvent.RegistrationSuccess -> {
                            navController.navigate(AppDestination.Patient.route) {
                                popUpTo(AppDestination.Login.route)
                            }
                        }
                        is RegistrationEvent.RegistrationFailure -> Unit
                    }
                }
            }

            RegistrationScreen(
                state = state,
                onNameChange = vm::onNameChange,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onRegisterClick = vm::register,
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Patient.route) {
            val vm: DashboardViewModel = viewModel()

            LifecycleResumeEffect(Unit) {
                vm.refresh()
                onPauseOrDispose { }
            }

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        DashboardEvent.LogoutSuccess -> {
                            navController.navigate(AppDestination.Login.route) {
                                popUpTo(AppDestination.Patient.route) { inclusive = true }
                            }
                        }
                        is DashboardEvent.LogoutFailure -> Unit
                        DashboardEvent.RefreshCompleted -> Unit
                        is DashboardEvent.RefreshFailed -> Unit
                    }
                }
            }

            PatientNavScreen(
                dashboardState = vm.state,
                onLogout = authViewModel::logout,
                navigateToExplanation = { documentId ->
                    navController.navigate("analysis_explanation/$documentId")
                },
                navigateToReportResults = { reportId ->
                    navController.navigate("report_results/$reportId")
                }
            )
        }

        composable(
            route = AppDestination.AnalysisExplanation.ROUTE_PATTERN,
            arguments = listOf(navArgument(AppDestination.AnalysisExplanation.ARG_DOCUMENT_ID) { type = NavType.StringType })
        ) {
            val vm: AnalysisExplanationViewModel = viewModel()

            LifecycleResumeEffect(Unit) {
                vm.refresh()
                onPauseOrDispose { }
            }

            val state by vm.state.collectAsStateWithLifecycle()

            AnalysisExplanationScreen(state = state, onClose = { navController.popBackStack() })
        }

        composable(
            route = AppDestination.ReportResults.ROUTE_PATTERN,
            arguments = listOf(navArgument(AppDestination.ReportResults.ARG_REPORT_ID) { type = NavType.StringType })
        ) {
            val vm: ReportResultsViewModel = viewModel()

            LifecycleResumeEffect(Unit) {
                vm.loadResults()
                onPauseOrDispose { }
            }

            val state by vm.state.collectAsStateWithLifecycle()

            ReportResultsScreen(
                state = state,
                onClose = { navController.popBackStack() },
                onRetry = { vm.loadResults() }
            )
        }
    }
}
