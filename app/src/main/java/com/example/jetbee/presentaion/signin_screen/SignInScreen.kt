package com.example.jetbee.presentaion.signin_screen

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jetbee.R
import com.example.jetbee.domain.model.AuthUser
import com.example.jetbee.navigation.Screens
import com.example.jetbee.presentaion.common.AuthenticationField
import com.example.jetbee.presentaion.common.RegularFont
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun SignInScreen(
    oneTapSignInViewModel: OneTapSignInViewModel = hiltViewModel(),
    signInViewModel: FirebaseSingInViewModel = hiltViewModel(),
    navController: NavController,
) {


    val signInState = signInViewModel.signInState.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }
    val isUserExist = signInViewModel.currentUserExist.collectAsState(initial = true)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val icon = if (passwordVisibility) {
        painterResource(id = R.drawable.password_visible)
    } else {
        painterResource(id = R.drawable.password_toggle)
    }

    LaunchedEffect(key1 = Unit) {
        if (isUserExist.value) {
            navController.popBackStack()
           navController.navigate(
               Screens.HomeScreen.route
           )
        }
    }

    if (!isUserExist.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 30.dp, end = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 15.dp),
                text = "Welcome Back",
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                fontFamily = RegularFont,
            )
            Text(
                text = "Log in to Continue",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp, color = Color.Gray,
                fontFamily = RegularFont,

                )
            AuthenticationField(
                text = email,
                placeHolder = "Email",
                isPasswordTextField = false,
                onValueChange = { email = it },
                errorMsg = "*Enter valid email address",
                trailingIcon = {
                    if (email.isNotBlank()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Text"
                            )

                        }
                    }
                })
            Spacer(modifier = Modifier.height(16.dp))
            AuthenticationField(
                text = password,
                placeHolder = "Password",
                isPasswordTextField = !passwordVisibility,
                onValueChange = { password = it },
                errorMsg = "*Enter valid password",
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(
                            painter = icon,
                            contentDescription = "Visibility Icon",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            Text(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 20.dp, top = 10.dp),
                text = "Forgot Password?",
                fontWeight = FontWeight.SemiBold, color = Color.Red, fontFamily = RegularFont,

                )
            Button(
                onClick = {
                    scope.launch(Dispatchers.Main) {
                        signInViewModel.loginUser(
                            AuthUser(
                                email, password
                            )
                        )
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 30.dp, end = 30.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    text = "Sign In",
                    color = Color.White,
                    modifier = Modifier
                        .padding(7.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (signInState.value?.isLoading == true) {
                    CircularProgressIndicator()
                }
            }
            Text(
                modifier = Modifier
                    .padding(15.dp)
                    .clickable {
                        navController.navigate(
                            Screens.FireSignUpScreen.route
                        )
                    },
                text = "Don't have an account? sign up",
                fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = RegularFont
            )
            Text(
                modifier = Modifier
                    .padding(
                        top = 40.dp,
                    ),
                text = "Or connect with",
                fontWeight = FontWeight.Medium, color = Color.Gray
            )
        }

    }


    LaunchedEffect(key1 = signInState.value?.error) {
        scope.launch(Dispatchers.Main) {
            if (signInState.value?.error?.isNotEmpty() == true) {
                val error = signInState.value?.error
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(key1 = signInState.value?.isSignedIn) {
        if (signInState.value?.isSignedIn?.isNotEmpty() == true) {
            navController.popBackStack()
            val successful = signInState.value?.isSignedIn
            Toast.makeText(context, successful, Toast.LENGTH_LONG).show()
            navController.navigate(
                Screens.HomeScreen.route
            )
        }
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credentials =
                        oneTapSignInViewModel.oneTapClient.getSignInCredentialFromIntent(result.data)
                    val googleIdToken = credentials.googleIdToken
                    val googleCredentials = getCredential(googleIdToken, null)
                    oneTapSignInViewModel.signInWithGoogle(googleCredentials)
                } catch (it: ApiException) {
                    print(it)
                }
            }
        }

    fun launch(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }
}


/*
@Preview(showBackground = true)
@Composable
fun PrevSignInScreen() {
    SignInScreen(navController = NavHostController(LocalContext.current))
}*/
