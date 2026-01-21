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
        modifier = modifier
    ) {
        // Home screen with special vertical transition
        composable(
            route = Screen.HOME,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, easing = EaseOutCubic)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(400, easing = EaseInCubic)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(500, easing = EaseOutCubic)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                onCreateSplit = {
                    navController.navigate(Screen.CREATE_SPLIT)
                },
                onHistoryClick = {
                    // TODO: Navigate to history screen when implemented
                }
            )
        }

        // Create split screen - slides up from bottom
        composable(
            route = Screen.CREATE_SPLIT,
            enterTransition = {
                fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, easing = EaseOutCubic)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) +
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(350, easing = EaseInCubic)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(350)) +
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(400, easing = EaseOutCubic)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400, easing = EaseInCubic)
                )
            }
        ) {
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

        // Review share screen - horizontal slide
        composable(
            route = Screen.REVIEW_SHARE,
            enterTransition = {
                fadeIn(animationSpec = tween(350)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450, easing = EaseOutCubic)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) +
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(350)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400, easing = EaseInCubic)
                )
            }
        ) {
            ReviewShareScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFinish = {
                    navController.popBackStack(Screen.HOME, inclusive = false)
                }
            )
        }

        // Contact picker - slides up as modal
        composable(
            route = Screen.CONTACT_PICKER,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400, easing = EaseOutCubic)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350, easing = EaseInCubic)
                )
            }
        ) {
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
