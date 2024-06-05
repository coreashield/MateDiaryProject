package com.example.matediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.matediary.ui.theme.MateDiaryTheme

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
