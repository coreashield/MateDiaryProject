package com.example.matediary

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar

class DailyDiary : ComponentActivity() {}

@Composable
fun DiaryScreen(date: String?, navController: NavHostController, supabase: SupabaseClient) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var diaryData by remember { mutableStateOf("") }
    var diaryLogs by remember { mutableStateOf(emptyList<DiaryLog>()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { imageUri = it }
        }
    )
    val context = LocalContext.current

    //선택 된 날짜에 일기 데이터가 있다면 일기 데이터 변수에 담기
    LaunchedEffect(key1 = Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val logs = DiarygGetData(date)
            if (logs.isNotEmpty()) {
                diaryData = logs[0].diary
            } else {
                diaryData = ""
            }
            diaryLogs = logs
        }
    }

    //DB 데이터 요청
    val insertInfo = createDiaryLog(
        inputUser = "jang",
        inputdiary = diaryData,
        inputCreated_at = date.toString()
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }

            Spacer(modifier = Modifier.width(40.dp))
            Text(
                text = "$date 일기",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(color = Color.LightGray, thickness = 3.dp)
        Spacer(modifier = Modifier.height(20.dp))
        MainImaeSelect(imageUri, launcher)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = diaryData,
            onValueChange = { diaryData = it },
            modifier = Modifier.height(200.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    if (diaryData.isEmpty()) {
                        Toast.makeText(context, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            supabase.from("DailyLog").insert(insertInfo) {
                            }
                        }
                        Toast.makeText(context, "$date 일기가 저장되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(64.dp)
            ) {
                Text(text = "등록")
            }

            Spacer(modifier = Modifier.width(30.dp))

            OutlinedButton(
                onClick = { /* Clear diary data */ },
                modifier = Modifier
                    .size(64.dp)
            ) {
                Text(text = "삭제")
            }
        }

        DiaryTable(diaryLogs)
        // 일기 테이블 표시

    }
}


@Composable
fun MainImaeSelect(
    imageUri: Uri?,
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
) {
    val imgMaxHeight = 440
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val halfScreenWidth = screenWidth / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(halfScreenWidth.dp)
    ) {
        // 기존 이미지
        Image(
            painter = painterResource(id = R.drawable.upload_img),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(imgMaxHeight.dp)
                .clip(RoundedCornerShape(0.dp))
                .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentScale = ContentScale.Fit
        )

        // 선택된 이미지 (겹치는 이미지)
        Image(
            painter = rememberImagePainter(imageUri), // selectedImagePainter는 선택된 이미지를 로드하는 Painter여야 합니다.
            contentDescription = null,
            modifier = Modifier
                .width(halfScreenWidth.dp)
                .height((imgMaxHeight - 70).dp)
//                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                .clip(RoundedCornerShape(0.dp))
                .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 한다.
            contentScale = ContentScale.Crop
        )
    }
}

@Serializable
data class DiaryLog(
    val user: String,
    val diary: String,
    val created_at: String,
)

fun createDiaryLog(
    inputUser: String,
    inputdiary: String,
    inputCreated_at: String,
): DiaryLog {
    return DiaryLog(
        user = inputUser,
        diary = inputdiary,
        created_at = inputCreated_at,
    )
}

suspend fun DiarygGetData(date: String?): List<DiaryLog> {
    val supabase = createSupabaseClient()

    // yyyyMMdd 형식으로 변환
    val formattedDate = date?.let {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd") // 기존 형식
        val outputFormat = SimpleDateFormat("yyyyMMdd") // 원하는 형식
        val parsedDate = inputFormat.parse(it)
        outputFormat.format(parsedDate)
    }

    val nextDate = formattedDate?.let {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(it)
        calendar.add(Calendar.DATE, 1)
        sdf.format(calendar.time)
    }

    val result = supabase.from("DailyLog")
//        .select(columns = Columns.list("diary"))
        .select()
        {
            filter {
                and {
                    eq("user", "jang")
                    DiaryLog::created_at eq  formattedDate
                    DiaryLog::created_at lt nextDate
                }
            }
        }.decodeAs<List<DiaryLog>>()

    return result
}

@Composable
fun DiaryTable(diaryLogs: List<DiaryLog>) {
    Column {
        // 데이터 행
        diaryLogs.forEach { log ->
            Row(Modifier
                .fillMaxWidth()) {
                Text(log.diary, Modifier.padding(8.dp))
            }
        }
    }
}

