package com.mobelio.bill.split.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobelio.bill.split.presentation.screens.contactpicker.ContactPickerScreen
import com.mobelio.bill.split.presentation.screens.createsplit.CreateSplitScreen
import com.mobelio.bill.split.presentation.screens.home.HomeScreen
import com.mobelio.bill.split.presentation.screens.reviewshare.ReviewShareScreen

@Composable
fun BillSplitNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(400, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(400, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Screen.HOME) {
            HomeScreen(
                onCreateSplit = {
                    navController.navigate(Screen.CREATE_SPLIT)
                },
                onHistoryClick = {
                    // TODO: Navigate to history screen when implemented
                }
            )
        }

        composable(Screen.CREATE_SPLIT) {
            CreateSplitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReview = {
                    navController.navigate(Screen.REVIEW_SHARE)
                },
                onNavigateToContactPicker = {
                    navController.navigate(Screen.CONTACT_PICKER)
                }
            )
        }

        composable(Screen.REVIEW_SHARE) {
            ReviewShareScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFinish = {
                    navController.popBackStack(Screen.HOME, inclusive = false)
                }
            )
        }

        composable(Screen.CONTACT_PICKER) {
            ContactPickerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactsSelected = {
                    navController.popBackStack()
                }
            )
        }
    }
}

