package com.example.matediary

import DiaryGetData
import SupabaseClient
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.matediary.ui.theme.EnlargedImageView
import com.example.matediary.ui.theme.MateDiaryTheme
import com.example.matediary.ui.theme.PhotoAlbumScreen
import getFileUrlFromSupabase
import getMateData
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
        installSplashScreen()

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
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Center로 변경
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(key1 = Unit) {

            CoroutineScope(Dispatchers.IO).launch {
                getMateData().takeIf { it.isNotEmpty() }?.let {

                    val age =
                        calculateAge(it[0].year.toInt(), it[0].month.toInt(), it[0].day.toInt())

                    cardData = CardPofileData(
                        imageUri = "", // 필요한 경우 여기에 값을 넣습니다.
                        imageDescription = it[0].name,
                        name = "${it[0].name}",
                        age = "${age}살",
                        description = "${it[0].year}년 ${it[0].month}월 ${it[0].day}일"
                    )
                }

                getFileUrlFromSupabase("infoImg", "jang/infoImg.jpg") { url ->
                    imageUrl = url
                    // cardData를 업데이트하여 imageUri를 포함시킵니다.
                    cardData = cardData.copy(imageUri = imageUrl)
                    // 데이터 읽었을 때 로딩완료.
                    isLoading = false
                }
            }
        }

        if (isLoading) {
            // 로딩 중일 때 표시할 화면
            CircularProgressIndicator()
        } else {
            // 데이터가 로딩된 후 표시할 화면
            CardProfileItem(cardData)
            CalendarView(navController)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(navController: NavController) {
    // 초기값은 오늘 날짜로 지정
    val selectedDate = remember { mutableStateOf(todayDate()) }
    var diaryLogText by remember { mutableStateOf<List<DiaryLog>?>(null) }

    // 전체화면 비율에 따라 달력, 네비게이션 크기 수정
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val desiredHeight = screenHeight.dp

    Scaffold(
        floatingActionButton = {
            Column {
                SmallFloatingActionButton(
                    onClick = { navController.navigate("diary/${selectedDate.value}") },
                    containerColor = colors.secondaryVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Create,
                        contentDescription = "",
                        tint = Color.White,
                    )
                }
                SmallFloatingActionButton(
                    onClick = { navController.navigate("gallery/${selectedDate.value}") },
                    containerColor = colors.secondaryVariant,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBox,
                        contentDescription = "",
                        tint = Color.White,
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationButtons(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(paddingValues)
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
                            val diaryData = DiaryGetData(selectedDate.value)
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

                val diaries: List<String>? = diaryLogText?.map { it.diary }
                val mainImgPathes: List<String>? = diaryLogText?.map { it.mainIMGpath }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DiaryTable(diaries, mainImgPathes, selectedDate.value, navController)
                }
            }
        }
    }
}

@Composable
fun Navigator() {
    val supabase = SupabaseClient.client
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    // 스플래시 화면 표시 여부 확인
    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        // 네비게이션 호스트 설정
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
                date?.let { PhotoAlbumScreen(navController, it) }
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
}

@Composable
fun BottomNavigationButtons(navController: NavController) {
    // 네비게이션 항목 리스트
    val items = listOf(
        Screen.Home,
        Screen.Gallery,
        Screen.Settings
    )

    BottomNavigation {
        // 현재 네비게이션 상태 가져오기
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // 항목 리스트를 순회하며 네비게이션 버튼 생성
        items.forEach { screen ->
            BottomNavigationItem(
                modifier = Modifier.background(color = Color.LightGray), // 배경 색상 지정
                icon = {
                    when (screen) {
                        Screen.Home -> Icon(Icons.Default.DateRange, contentDescription = null) // 홈 아이콘
                        Screen.Gallery -> Icon(Icons.Default.AccountCircle, contentDescription = null) // 갤러리 아이콘
                        Screen.Settings -> Icon(Icons.Default.Settings, contentDescription = null) // 설정 아이콘
                    }
                },
                label = { Text(screen.title) }, // 항목 제목
                selected = currentDestination?.route == screen.route, // 현재 선택된 항목인지 확인
                onClick = {
                    navController.navigate(screen.route) {
                        // 동일한 경로로 이동할 때 뒤로가기 스택에서 중복 방지
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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
fun DiaryTable(
    diaries: List<String>?,
    mainImgPath: List<String>?,
    date: String,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Top // 수직 상단 정렬
    ) {
        // 다이어리 리스트가 비어있을 경우를 처리
        itemsIndexed(diaries.orEmpty()) { index, diary ->
            val diaryData = CardDiaryData(
                imageUri = mainImgPath?.getOrNull(index) ?: "", // 이미지 경로가 없을 경우 빈 문자열 사용
                diary = diary,
                date = date,
            )
            CardDiaryItem(diaryData, navController) // 각 다이어리 아이템을 CardDiaryItem 컴포저블로 표시
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
    var imageUri: String,
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
    var showEnlargedImage by remember { mutableStateOf(false) }
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
            horizontalArrangement = Arrangement.Start
        ) {
            if (cardData.name == "") {
                Text("등록 된 메이트 정보 없음!")
            } else {
                AsyncImage(
                    model = cardData.imageUri?.takeIf { it.isNotEmpty() } ?: "",
                    placeholder = ColorPainter(Color.Gray), // 이미지가 없을 때 회색 표시
                    contentScale = ContentScale.Crop,
                    contentDescription = "",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable {
                            cardData.imageUri?.let {
                                showEnlargedImage = true
                            }
                        }
                )

                Spacer(modifier = Modifier.size(16.dp))
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cardData.name
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = cardData.age
                    )
                }

            }
        }
    }

    if (showEnlargedImage) {
        EnlargedImageView(
            imageUrl = cardData.imageUri ?: "",
            onClose = { showEnlargedImage = false },
        )
    }
}

@Composable
fun CardDiaryItem(
    diaryData: CardDiaryData,
    navController: NavController
) {
    val placeholderColor = Color(0x33000000) // 회색

    Card(
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp) // 수평 및 수직 여백 설정
            .clickable {
                // 카드 클릭 시, diaryData.date와 diaryData.diary 값을 포함하여 네비게이션
                val encodedDiary = URLEncoder
                    .encode(diaryData.diary, StandardCharsets.UTF_8.toString())
                    .replace("+", " ")
                navController.navigate("diary/${diaryData.date}?diary=$encodedDiary")
            }
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // 수평 정렬을 시작점으로 변경
        ) {
            // 비동기 이미지 로딩
            AsyncImage(
                model = diaryData.imageUri.takeIf { it.isNotEmpty() } ?: "",
                placeholder = ColorPainter(placeholderColor), // 이미지가 없을 때 표시할 색상
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.size(8.dp))

            // 다이어리 내용 표시
            Column {
                Text(
                    text = diaryData.diary,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis // 텍스트가 길 때 줄임표 처리
                )
            }
        }
    }
}
