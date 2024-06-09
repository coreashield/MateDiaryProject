package com.example.matediary

import ImageItem
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import createSupabaseClient
import getFileUrlFromSupabase
import getImageList
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uploadFileToSupabase
import java.text.SimpleDateFormat
import java.util.Calendar

class DailyDiary : ComponentActivity() {}

@Composable
fun DiaryScreen(date: String?, navController: NavHostController, supabase: SupabaseClient) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var diaryData by remember { mutableStateOf("") }
    var diaryLogs by remember { mutableStateOf(emptyList<DiaryLog>()) }
    var imageList by remember { mutableStateOf(emptyList<ImageItem>()) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { imageUri = it }
        }
    )
    // 선택된 날짜에 일기 데이터 요청
    LaunchedEffect(key1 = Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val logs = DiarygGetData(date)
            diaryData = logs.firstOrNull()?.diary ?: ""
            diaryLogs = logs
        }

        // 대표 이미지
        val fileName = "jang/$date/main.jpg"
        getFileUrlFromSupabase("album", fileName) { url ->
            imageUrl = url
        }
//        imageList = getImageList(folderPath = "jang/$date")
    }

    // DB 일기 데이터 정보
    val insertInfo = createDiaryLog(
        inputUser = "jang",
        inputdiary = diaryData,
        inputCreated_at = date.toString()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$date 일기",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(thickness = 3.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(20.dp))
        ImageUploadIcon(imageUri, launcher, imageUrl)
        Spacer(modifier = Modifier.height(20.dp))

        Column {
            imageList.chunked(3).forEach { rowImages ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowImages.forEach { imageItem ->
                        val itemUrl =
                            "https://${BuildConfig.API_URL}.supabase.co/storage/v1/object/public/album/jang/${imageItem.name}"
//                            Text(imageItem.name)
                        Image(
                            painter = rememberAsyncImagePainter(itemUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        OutlinedTextField(
            value = diaryData,
            onValueChange = { diaryData = it },
            modifier = Modifier
                .height(200.dp)
                .width(300.dp),
            placeholder = { Text(text = "일기를 입력해주세요") },
            maxLines = 7,
        )

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.28f)) //버튼 오른쪽 여백
            Button(
                onClick = {
                    if (diaryData.isEmpty()) {
                        Toast.makeText(context, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            supabase.from("DailyLog").insert(insertInfo)

                            // 이미지 업로드
                            imageUri?.let { uri ->
                                //해당 날짜의 대표 이미지는 main.jpg로 저장
                                uploadFileToSupabase(
                                    context, "album",
                                    "jang/$date/main", uri
                                )
                            }

                        }
                        Toast.makeText(context, "$date 일기가 저장되었습니다!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "등록")
            }

            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        supabase.from("DailyLog").delete {
                            filter {
                                eq("user", "jang")
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "삭제")
            }
            Spacer(modifier = Modifier.weight(0.28f))
        }

        Row(){
            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }

            Column {
                imageList.chunked(3).forEach { rowImages ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowImages.forEach { imageItem ->
                            val itemUrl =
                                "https://${BuildConfig.API_URL}.supabase.co/storage/v1/object/public/album/jang/${imageItem.name}"
//                            Text(imageItem.name)
                            Image(
                                painter = rememberAsyncImagePainter(itemUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "back")
            }
        }
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
        calendar.time = sdf.parse(it)!!
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
                    DiaryLog::created_at eq formattedDate
                    DiaryLog::created_at lt nextDate
                }
            }
        }.decodeAs<List<DiaryLog>>()

    return result
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ImageUploadIcon(
    imageUri: Uri?,
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    imageUrl: String?
) {
    Column {
        IconButton(onClick = {
            // 이미지를 선택하는 런처를 실행합니다.
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        ) {
            Icon(
                imageVector = Icons.Outlined.AddCircle,
                contentDescription = "Localized description",
                tint = Color.Black
            )
        }
    }

    //이미 등록 된 대표 이미지가 있을 경우
    val painter = if (imageUri != null) {
        rememberAsyncImagePainter(imageUri)
    } else {
        rememberAsyncImagePainter(imageUrl)
    }
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(24.dp)),
        contentScale = ContentScale.Crop
    )
}


