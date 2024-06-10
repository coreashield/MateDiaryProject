package com.example.matediary

import DiarygGetData
import SupabseClient
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.matediary.ui.theme.MateDiaryTheme
import getData
import getFileUrlFromSupabase
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

object CardDataHolder {
    val defaultCardData = CardData(
        imageUri = "",
        imageDescription = "",
        name = "",
        description = ""
    )
}

@Composable
fun MainCalenderView(navController: NavHostController) {
    var mateName by remember { mutableStateOf("") }
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
                    mateName = it[0].name
                    cardData = CardData(
                        imageUri = "", // 필요한 경우 여기에 값을 넣습니다.
                        imageDescription = it[0].name,
                        name = it[0].name,
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

//        Spacer(modifier = Modifier.height(15.dp))
        Column(verticalArrangement = Arrangement.Center) {
            CardItem(cardData)
            CalendarView(navController)
        }
    }
}


@Composable
fun CalendarView(navController: NavController) {
    //초기값은 오늘 날짜로 지정
    val selectedDate = remember { mutableStateOf(todayDate()) }
    var diaryLogText by remember { mutableStateOf<List<DiaryLog>?>(null) }

    //전체화면 비율에 따라 달력, 네비게이션 크기 수정
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val desiredHeight = (screenHeight * 0.93).dp

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
//                .padding(8.dp)
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
                CalendarView(context).apply {
                    val today = Calendar.getInstance()
                    date = today.timeInMillis

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val date = "$year-${month + 1}-$dayOfMonth"
                        selectedDate.value = date
                    }
                }
            })
            //, modifier = Modifier.padding(top = 20.dp))


            //날짜를 클릭했을 때만 버튼 활성화
            val diaries: List<String>? = diaryLogText?.map { it.diary }
            if (diaries != null) {
                MoveActivity(navController, selectedDate.value)
                DiaryTable(diaries)
            }
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom){
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
        composable("diary/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DiaryScreen(date, navController, supabase)
        }

        composable("diary") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            DiaryScreen(date, navController, supabase)
        }
    }
}


@Composable
fun BottomNavigationButtons(navController: NavController) {
    val items = listOf(
        Screen.Home,
//        Screen.Gallery,
        Screen.Settings
    )

    // Bottom Navigation
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
fun DiaryTable(diaries: List<String>?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "번호",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 16.dp),
            )
            Text(
                text = "일기 내용",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp),
            )
        }
    }

    LazyColumn(
        // 화면을 가득 채우도록 설정
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally // 수평 가운데 정렬
    ) {
        // 일기 데이터 행 추가
        diaries?.forEachIndexed { index, diary ->
            item {
                // 일기 번호와 내용을 표시
                LimitedText("\t\t\t\t${index + 1}\t\t\t\t\t\t$diary", 15)
            }
        }
    }
}

@Composable
fun LimitedText(text: String, maxLength: Int) {
    val displayedText = if (text.length > maxLength) {
        text.take(maxLength) + "..." // 최대 길이 초과 시 잘라내고 "..." 추가
    } else {
        text // 최대 길이를 초과하지 않으면 원래 텍스트 표시
    }

    Text(
        text = displayedText,
    )
}

@Composable
fun MoveActivity(navController: NavController, date: String) {
    Row(
        modifier = Modifier
            .padding(end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = date,
            modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
        ElevatedButton(onClick = { navController.navigate("diary/$date") },
            modifier = Modifier.weight(1F)) {
            Text("일기장")
        }
        Spacer(modifier = Modifier.width(10.dp))
        ElevatedButton(onClick = { navController.navigate("gallery/$date") },
            modifier = Modifier.weight(1F)) {
            Text("갤러리")
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

data class CardData(
    val imageUri: String,
    val imageDescription: String,
    val name: String,
    val description: String,
)

@Composable
fun CardItem(cardData: CardData) {
    val color = Color(0x33000000)

    Card(
        elevation = 8.dp,
//        modifier = Modifier.padding(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),

            ) {
            AsyncImage(
                model = cardData.imageUri,
                placeholder = ColorPainter(color), // 이미지가 없을때 넣을 것
                contentScale = ContentScale.Crop, // 사이즈에 맞지 않은 것은 잘라냄
                contentDescription = "sleeping cat",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape), // 둥굴게,
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column {
                Text(
                    text = cardData.name,
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = cardData.description,
                )
            }
        }
    }
}

