package com.example.matediary

import DiarygGetData
import SupabseClient
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.matediary.ui.theme.MateDiaryTheme
import getData
import getFileUrlFromSupabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.Period
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

object CardDataHolder {
    val defaultCardData = CardPofileData(
        imageUri = "",
        imageDescription = "",
        name = "",
        age = "",
        description = ""
    )
}


@Composable
fun MainCalenderView(navController: NavHostController) {
    var cardData by remember { mutableStateOf(CardDataHolder.defaultCardData) }
    var imageUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Center로 변경
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(key1 = Unit) {

            CoroutineScope(Dispatchers.IO).launch {
                getData().takeIf { it.isNotEmpty() }?.let {

                    val age =
                        calculateAge(it[0].year.toInt(), it[0].month.toInt(), it[0].day.toInt())

                    cardData = CardPofileData(
                        imageUri = "", // 필요한 경우 여기에 값을 넣습니다.
                        imageDescription = it[0].name,
                        name = "이름 : ${it[0].name}",
                        age = "나이 : ${age}살",
                        description = "생일 : ${it[0].year}년 ${it[0].month}월 ${it[0].day}일"
                    )
                }

                getFileUrlFromSupabase("infoImg", "jang/infoImg.jpg") { url ->
                    imageUrl = url
                    // cardData를 업데이트하여 imageUri를 포함시킵니다.
                    cardData = cardData.copy(imageUri = imageUrl)
                }
            }
        }
        CardProfileItem(cardData)
        CalendarView(navController)
    }
}


@Composable
fun CalendarView(navController: NavController) {
    //초기값은 오늘 날짜로 지정
    val selectedDate = remember { mutableStateOf(todayDate()) }
    var diaryLogText by remember { mutableStateOf<List<DiaryLog>?>(null) }

    //전체화면 비율에 따라 달력, 네비게이션 크기 수정
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val desiredHeight = screenHeight.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RectangleShape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(desiredHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LaunchedEffect(selectedDate.value) {
                if (selectedDate.value.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val diaryData = DiarygGetData(selectedDate.value)
                        diaryLogText = diaryData
                    }
                }
            }
            AndroidView(factory = { context ->
                android.widget.CalendarView(context).apply {
                    val today = Calendar.getInstance()
                    date = today.timeInMillis
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val date = "$year-${month + 1}-$dayOfMonth"
                        selectedDate.value = date
                    }
                }
            })
            MyCircularDropdownMenu(navController, selectedDate.value)

            val diaries: List<String>? = diaryLogText?.map { it.diary }

            DiaryTable(diaries, selectedDate.value, navController)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                BottomNavigationButtons(navController)
            }

        }
    }
}


@Composable
fun Navigator() {
    val supabase = SupabseClient.client
    val navController = rememberNavController()
    NavHost(navController, startDestination = "calendar") {

        composable("calendar") {
            MainCalenderView(navController)
        }
        composable("gallery") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            GalleryView(date.toString(), navController)
        }

        composable("gallery/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            GalleryView(date.toString(), navController)
        }
        composable("mateinfo") {
            MainScreen(navController)
        }
        composable(
            "diary/{date}?diary={diary}",
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("diary") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            val diary = backStackEntry.arguments?.getString("diary") ?: ""
            DiaryScreen(
                date = date,
                diary = diary,
                navController = navController,
                supabase = supabase
            )
        }

        composable("diary/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DiaryScreen(date, "", navController, supabase)
        }
    }
}


@Composable
fun BottomNavigationButtons(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Settings
    )

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier
                    .background(color = Color.LightGray),
                icon = {
                    when (screen) {
                        Screen.Home -> Icon(Icons.Default.DateRange, contentDescription = null)
                        Screen.Gallery -> Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null
                        )

                        Screen.Settings -> Icon(Icons.Default.AccountBox, contentDescription = null)
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
    data object Home : Screen("calendar", "Home")
    data object Gallery : Screen("gallery", "갤러리")
    data object Settings : Screen("mateinfo", "정보")
}

@Composable
fun DiaryTable(diaries: List<String>?, date: String, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .height(160.dp)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 수평 가운데 정렬
    ) {
        itemsIndexed(diaries ?: emptyList()) { _, diary ->
            val diaryData = CardDiaryData(
                imageUri = "",
                diary = diary,
                date = date,
            )
            CardDiaryItem(diaryData, navController)
        }
    }
}


fun todayDate(): String {
    val calendar = Calendar.getInstance()
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${
        calendar.get(
            Calendar.DAY_OF_MONTH
        )
    }"
}

fun calculateAge(birthYear: Int, birthMonth: Int, birthDay: Int): Int {
    val birthDate = LocalDate.of(birthYear, birthMonth, birthDay)
    val currentDate = LocalDate.now()
    val age = Period.between(birthDate, currentDate).years
    return age
}

data class CardPofileData(
    val imageUri: String,
    val imageDescription: String,
    val name: String,
    val age: String,
    val description: String,
)

data class CardDiaryData(
    val imageUri: String,
    val diary: String,
    val date: String,
)

@Composable
fun CardProfileItem(cardData: CardPofileData) {
    val color = Color(0x33000000) //그레이

    Card(
        elevation = 8.dp,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            if (cardData.name == "") {
                Text("등록 된 메이트 정보 없음!")
            } else {
                AsyncImage(
                    model = cardData.imageUri,
                    placeholder = ColorPainter(color), // 이미지가 없을때
                    contentScale = ContentScale.Crop,
                    contentDescription = "",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${cardData.name} , ${cardData.age}"
                    )
                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        text = cardData.description,
                    )
                }
            }

        }
    }
}

@Composable
fun CardDiaryItem(diaryData: CardDiaryData, navController: NavController) {
    val color = Color(0x33000000) //그레이
    Card(
        elevation = 8.dp,
        modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            // 카드 클릭 시, diaryData.date와 diaryData.diary 값을 포함하여 네비게이션
            .clickable {
                val encodedDiary =
                    URLEncoder
                        .encode(diaryData.diary, StandardCharsets.UTF_8.toString())
                        .replace("+", " ")
                navController.navigate("diary/${diaryData.date}?diary=$encodedDiary")
            },
    ) {
        Row(
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center

        ) {
            AsyncImage(
                model = diaryData.imageUri,
                placeholder = ColorPainter(color), // 이미지가 없을때 넣을 것
                contentScale = ContentScale.Crop,
                contentDescription = "",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.size(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = diaryData.diary,
                    fontSize = 16.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun MyCircularDropdownMenu(navController: NavController, date: String) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("일기장 이동", "갤러리 이동")
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box{
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp),
                horizontalArrangement = Arrangement.Start){
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = Color.White,
                        ),
                ) {
                    Icon(
                        modifier = Modifier
                            .size(80.dp),
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "movePage",
                        tint = Color.Black
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    items.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                expanded = false
                                val route = when (index) {
                                    0 -> "diary/$date"
                                    1 -> "gallery/$date"
                                    else -> return@DropdownMenuItem
                                }
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }

        }
    }
}