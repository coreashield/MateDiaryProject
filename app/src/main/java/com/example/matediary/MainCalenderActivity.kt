package com.example.matediary

import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.matediary.ui.theme.MateDiaryTheme
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainCalenderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MateDiaryTheme {
                Navigator()
            }
        }
    }
}

@Composable
fun MainCalenderView(navController: NavHostController) {

    var mateName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Center로 변경
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(key1 = Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                if (getData().isNotEmpty()) {
                    mateName = getData()[0].name
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(text = "$mateName 메이트님 안녕하세요 :-)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        CalendarView(navController)
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            BottomNavigationButtons(navController)
        }
    }
}


@Composable
fun CalendarView(navController: NavController) {
    val selectedDate = remember { mutableStateOf("") }
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
            AndroidView(factory = { context ->
                CalendarView(context).apply {
                    date = today.timeInMillis

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val date = "$year-${month + 1}-$dayOfMonth"
                        selectedDate.value = date
                        navController.navigate("diary/$date")
                    }
                }
            }, modifier = Modifier.padding(top = 20.dp))
        }
    }
}


@Composable
fun Navigator() {
    val supabase = createSupabaseClient()
    val navController = rememberNavController()
    NavHost(navController, startDestination = "calendar") {
        composable("calendar") {
            MainCalenderView(navController)
        }
        composable("gallery") {
//            DiaryScreen(date)
        }
        composable("mateinfo") {  4
            MainScreen(navController)
        }

        composable("diary/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DiaryScreen(date,navController,supabase)
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


suspend fun getData(): List<MateInfo> {
    val supabase = createSupabaseClient()
    val result = supabase.from("mateinfo")
//        .select(columns = Columns.list("type", "name","year","month"))
        .select()
        {
            filter {
                eq("user", "jang")
            }
        }.decodeAs<List<MateInfo>>()

    return result
}