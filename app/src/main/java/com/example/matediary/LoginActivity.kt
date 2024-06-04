package com.example.matediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.matediary.ui.theme.MateDiaryTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
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

//supabaseUrl = "https://rooxnjbwwvkgbwlfdnen.supabase.co",
//supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJvb3huamJ3d3ZrZ2J3bGZkbmVuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTcxMjkyNjAsImV4cCI6MjAzMjcwNTI2MH0.b982U5hA2NqyXEoxqoEHoH18NQexwftWoXd0gHoObB4"

//superbase 키값이 없어 패
