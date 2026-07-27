package com.example.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.share.ui.main.ConsumerMainScreen
import com.example.share.ui.pickup.ConsumerPickupFormScreen
import com.example.share.ui.pickup.ConsumerPickupDetailScreen
import com.example.share.ui.fundraiser.ConsumerFundraiserDetailScreen
import com.example.share.ui.fundraiser.ConsumerCreateFundraiserScreen
import com.example.share.ui.volunteer.ConsumerVolunteerRegisterScreen
import com.example.share.ui.volunteer.ConsumerVolunteerDetailScreen
import com.example.share.ui.auth.LoginScreen
import com.example.share.ui.auth.OtpVerificationScreen
import com.example.share.ui.auth.RegisterDetailsScreen

@Composable
fun MainNavigation() {
  val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
  val startDestination = if (currentUser != null) Main else Login
  val backStack = rememberNavBackStack(startDestination)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(
            onNavigateToOtp = { contact ->
              backStack.add(OtpVerification(contact))
            }
          )
        }
        entry<OtpVerification> { key ->
          OtpVerificationScreen(
            contact = key.contact,
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToHome = {
              // Clear onboarding backstack
              backStack.removeLastOrNull() // remove Otp
              backStack.removeLastOrNull() // remove Login
              backStack.add(Main)
            },
            onNavigateToRegisterDetails = { contact ->
              backStack.add(RegisterDetails(contact))
            }
          )
        }
        entry<RegisterDetails> { key ->
          RegisterDetailsScreen(
            contact = key.contact,
            onNavigateToHome = {
              // Clear onboarding backstack
              backStack.removeLastOrNull() // remove Details
              backStack.removeLastOrNull() // remove Otp
              backStack.removeLastOrNull() // remove Login
              backStack.add(Main)
            }
          )
        }
        entry<Main> {
          ConsumerMainScreen(onItemClick = { navKey -> backStack.add(navKey) })
        }
        entry<PickupForm> {
          ConsumerPickupFormScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToDetail = { id ->
              backStack.removeLastOrNull() // remove form from backstack
              backStack.add(PickupDetail(id))
            }
          )
        }
        entry<PickupDetail> { key ->
          ConsumerPickupDetailScreen(
            requestId = key.requestId,
            onNavigateBack = { backStack.removeLastOrNull() }
          )
        }
        entry<FundraiserDetail> { key ->
          ConsumerFundraiserDetailScreen(
            fundraiserId = key.fundraiserId,
            onNavigateBack = { backStack.removeLastOrNull() }
          )
        }
        entry<CreateFundraiser> {
          ConsumerCreateFundraiserScreen(onNavigateBack = { backStack.removeLastOrNull() })
        }
        entry<VolunteerRegister> {
          ConsumerVolunteerRegisterScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToDetail = { id ->
              backStack.removeLastOrNull() // remove form from backstack
              backStack.add(VolunteerDetail(id))
            }
          )
        }
        entry<VolunteerDetail> { key ->
          ConsumerVolunteerDetailScreen(
            volunteerId = key.volunteerId,
            onNavigateBack = { backStack.removeLastOrNull() }
          )
        }
      },
  )
}
