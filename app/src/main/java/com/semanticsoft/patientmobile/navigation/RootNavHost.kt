package com.semanticsoft.patientmobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.feature.auth.AuthEvent
import com.semanticsoft.patientmobile.feature.auth.AuthState
import com.semanticsoft.patientmobile.feature.auth.AuthViewModel
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.ForgotPasswordEvent
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.ForgotPasswordScreen
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.ForgotPasswordViewModel
import com.semanticsoft.patientmobile.feature.auth.login.LoginEvent
import com.semanticsoft.patientmobile.feature.auth.login.LoginScreen
import com.semanticsoft.patientmobile.feature.auth.login.LoginViewModel
import com.semanticsoft.patientmobile.feature.laborator.activateAccount.ActivateAccountEvent
import com.semanticsoft.patientmobile.feature.laborator.activateAccount.ActivateAccountScreen
import com.semanticsoft.patientmobile.feature.laborator.activateAccount.ActivateAccountViewModel
import com.semanticsoft.patientmobile.feature.laborator.dpa.DpaEvent
import com.semanticsoft.patientmobile.feature.laborator.dpa.DpaScreen
import com.semanticsoft.patientmobile.feature.laborator.dpa.DpaViewModel
import com.semanticsoft.patientmobile.feature.laborator.navigation.LaboratorNavScreen
import com.semanticsoft.patientmobile.feature.laborator.registration.RegistrationLabEvent
import com.semanticsoft.patientmobile.feature.laborator.registration.RegistrationLabScreen
import com.semanticsoft.patientmobile.feature.laborator.registration.RegistrationLabViewModel
import com.semanticsoft.patientmobile.feature.patient.analysisExplanation.AnalysisExplanationScreen
import com.semanticsoft.patientmobile.feature.patient.analysisExplanation.AnalysisExplanationViewModel
import com.semanticsoft.patientmobile.feature.patient.dashboard.DashboardEvent
import com.semanticsoft.patientmobile.feature.patient.dashboard.DashboardViewModel
import com.semanticsoft.patientmobile.feature.patient.navigation.PatientNavScreen
import com.semanticsoft.patientmobile.feature.patient.registration.RegistrationEvent
import com.semanticsoft.patientmobile.feature.patient.registration.RegistrationScreen
import com.semanticsoft.patientmobile.feature.patient.registration.RegistrationViewModel
import com.semanticsoft.patientmobile.feature.patient.reportResults.ReportResultsScreen
import com.semanticsoft.patientmobile.feature.patient.reportResults.ReportResultsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RootNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
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
            val vm: LoginViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is LoginEvent.LoginSuccess -> {
                            val destination = when (event.role) {
                                UserRole.LABORATOR -> AppDestination.Laborator
                                else -> AppDestination.Patient
                            }
                            navController.navigate(destination.route) {
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
                onLaboratorAccess = {
                    navController.navigate(AppDestination.LaboratorActivateAccount.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(AppDestination.PatientRegistration.route) },
                onGoToLabRegister = { navController.navigate(AppDestination.LaboratorRegistration.route) },
                onGoToForgotPassword = { navController.navigate(AppDestination.ForgotPassword.route) }
            )
        }

        composable(AppDestination.ForgotPassword.route) {
            val vm: ForgotPasswordViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        ForgotPasswordEvent.PasswordChanged -> {
                            Toast.makeText(context, "Parola schimbată cu succes", Toast.LENGTH_SHORT).show()
                            navController.navigate(AppDestination.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }

            ForgotPasswordScreen(
                state = state,
                onEmailChange = vm::onEmailChange,
                onRequestSubmit = vm::onRequestSubmit,
                onCodeChange = vm::onCodeChange,
                onResendClick = vm::onResendClick,
                onNewPasswordChange = vm::onNewPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onSubmitNewPassword = vm::onSubmitNewPassword,
                onRequestNewCode = vm::onRequestNewCode,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(AppDestination.PatientRegistration.route) {
            val vm: RegistrationViewModel = hiltViewModel()
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

        composable(AppDestination.LaboratorRegistration.route) {
            val vm: RegistrationLabViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        is RegistrationLabEvent.RegistrationSuccess -> {
                            navController.navigate(AppDestination.Laborator.route) {
                                popUpTo(AppDestination.Login.route)
                            }
                        }
                        is RegistrationLabEvent.RegistrationFailure -> Unit
                    }
                }
            }

            RegistrationLabScreen(
                state = state,
                onLabNameChange = vm::onLabNameChange,
                onCountryChange = vm::onCountryChange,
                onCuiChange = vm::onCuiChange,
                onContactNameChange = vm::onContactNameChange,
                onRoleChange = vm::onRoleChange,
                onEmailChange = vm::onEmailChange,
                onPhoneChange = vm::onPhoneChange,
                onMessageChange = vm::onMessageChange,
                onRegisterClick = vm::onRegisterClick,
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(AppDestination.LaboratorActivateAccount.route) {
            val vm: ActivateAccountViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        ActivateAccountEvent.Activated -> {
                            navController.navigate(AppDestination.LaboratorDpa.route) {
                                popUpTo(AppDestination.LaboratorActivateAccount.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            ActivateAccountScreen(
                state = state,
                onPasswordChange = vm::onPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onTermsAcceptedChange = vm::onTermsAcceptedChange,
                onActivateClick = vm::onActivateClick,
                onTermsClick = {},
                onPrivacyClick = {}
            )
        }

        composable(AppDestination.LaboratorDpa.route) {
            val vm: DpaViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.events.collectLatest { event ->
                    when (event) {
                        DpaEvent.ContinueToPortal -> {
                            navController.navigate(AppDestination.Laborator.route) {
                                popUpTo(AppDestination.LaboratorDpa.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            DpaScreen(
                state = state,
                onAcceptedChange = vm::onAcceptedChange,
                onContinueClick = vm::onContinueClick,
                onDownloadPdfClick = {}
            )
        }

        composable(AppDestination.Patient.route) {
            val vm: DashboardViewModel = hiltViewModel()

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

        composable(AppDestination.Laborator.route) {
            LaboratorNavScreen(onLogout = authViewModel::logout)
        }

        composable(
            route = AppDestination.AnalysisExplanation.ROUTE_PATTERN,
            arguments = listOf(navArgument(AppDestination.AnalysisExplanation.ARG_DOCUMENT_ID) { type = NavType.StringType })
        ) {
            val vm: AnalysisExplanationViewModel = hiltViewModel()

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
            val vm: ReportResultsViewModel = hiltViewModel()

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
