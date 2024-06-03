package com.example.matediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.matediary.ui.theme.MateDiaryTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            MateDiaryTheme {
                MainLogin()
            }
        }
    }
}
@Composable
fun MainLogin(){
    Column {
        Text(text = "로그인")
    }
}

@Preview(showBackground = true)
@Composable
fun MainLoginPreview() {
    MainLogin()
}

//superbase 키값이 없어 패스
//@Composable
//fun GoogleSignInButton() {
//    val coroutineScope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    val onClick: () -> Unit = {
//        val credentialManager = CredentialManager.create(context)
//
//        // Generate a nonce and hash it with sha-256
//        // Providing a nonce is optional but recommended
//        val rawNonce = UUID.randomUUID().toString() // Generate a random String. UUID should be sufficient, but can also be any other random string.
//        val bytes = rawNonce.toString().toByteArray()
//        val md = MessageDigest.getInstance("SHA-256")
//        val digest = md.digest(bytes)
//        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) } // Hashed nonce to be passed to Google sign-in
//
//
//        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false)
//            .setServerClientId("WEB_GOOGLE_CLIENT_ID")
//            .setNonce(hashedNonce) // Provide the nonce if you have one
//            .build()
//
//        val request: GetCredentialRequest = GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//
//        coroutineScope.launch {
//            try {
//                val result = credentialManager.getCredential(
//                    request = request,
//                    context = context,
//                )
//
//                val googleIdTokenCredential = GoogleIdTokenCredential
//                    .createFrom(result.credential.data)
//
//                val googleIdToken = googleIdTokenCredential.idToken
//
//                supabase.auth.signInWith(IDToken) {
//                    idToken = googleIdToken
//                    provider = Google
//                    nonce = rawNonce
//                }
//
//                // Handle successful sign-in
//            } catch (e: GetCredentialException) {
//                // Handle GetCredentialException thrown by `credentialManager.getCredential()`
//            } catch (e: GoogleIdTokenParsingException) {
//                // Handle GoogleIdTokenParsingException thrown by `GoogleIdTokenCredential.createFrom()`
//            } catch (e: RestException) {
//                // Handle RestException thrown by Supabase
//            } catch (e: Exception) {
//                // Handle unknown exceptions
//            }
//        }
//    }
//
//    Button(
//        onClick = onClick,
//    ) {
//        Text("Sign in with Google")
//    }
//}
