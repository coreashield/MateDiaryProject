package com.example.matediary

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.matediary.ui.theme.MateDiaryTheme
import java.util.Calendar

class MainCalenderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            MateDiaryTheme {
                Navigator()
            }
        }
    }
}

@Composable
fun MainCalenderView(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Center로 변경
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "달력 이미지", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        CalendarViewDemo()

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            BottomNavigationButtons(navController)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainCalenderViewPreview() {
//    MainCalenderView(navController)
}


@Composable
fun CalendarViewDemo() {
    // Holds state
    val selectedDate = remember { mutableStateOf("") }

    // Get today's date
    val today = Calendar.getInstance()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RectangleShape
            )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // CalendarView
            AndroidView(factory = { context ->
                CalendarView(context).apply {
                    // Set today's date as default
                    setDate(today.timeInMillis)

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedDate.value = "$year/${month + 1}/$dayOfMonth"
                    }
                }
            }, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
fun Navigator() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "calendar") {
        composable("calendar") {
            MainCalenderView(navController)
        }
        composable("gallery") {

        }
        composable("mateinfo") {  4
            MainScreen()
        }
        composable("diary"){

        }
    }
}


@Composable
fun BottomNavigationButtons(navController: NavController) {

    // Bottom Navigation 아이템들
    val items = listOf(
        Screen.Home,
        Screen.Gallery,
        Screen.Settings
    )

    // Bottom Navigation
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.background(color = Color.LightGray),
                icon = {
                    when (screen) {
                        Screen.Home -> Icon(Icons.Default.DateRange, contentDescription = null)
                        Screen.Gallery -> Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null
                        )

                        Screen.Settings -> Icon(Icons.Default.Person, contentDescription = null)
                    }
                },
                label = { Text(screen.title) },
                selected = currentDestination?.route == screen.route,
                onClick = {
                         navController.navigate(screen.route) {
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("calendar", "Home")
    object Gallery : Screen("gallery", "갤러리")
    object Settings : Screen("mateinfo", "정보")
}


