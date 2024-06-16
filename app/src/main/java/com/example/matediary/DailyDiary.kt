package com.example.matediary

import DiaryGetData
import ImageItem
import SupabaseClient.deleteFileFromSupabaseLaunchIO
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uploadFileToSupabase

class DailyDiary : ComponentActivity() {}

@Composable
fun DiaryScreen(
    date: String?,
    diary: String,
    navController: NavHostController,
    supabase: SupabaseClient,
) {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var diaryData by remember { mutableStateOf(diary) }
    var imageList by remember { mutableStateOf(emptyList<ImageItem>()) }
    var diaryLogs by remember { mutableStateOf(emptyList<DiaryLog>()) }

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
            val logs = DiaryGetData(date)
            //넘긴 diary값이 있으면 db값 확인, 없으면 diary값으로
            if (diary == "") {
                diaryData = logs.firstOrNull()?.diary ?: ""
            } else {
                diaryData = diary
            }
            diaryLogs = logs
        }


        // 일기 대표 이미지
        val fileName = "jang/$date/main.jpg"
        getFileUrlFromSupabase("album", fileName) { url ->
            imageUrl = url
        }
    }

    // DB 일기 데이터 정보
    val insertInfo = createDiaryLog(
        inputUser = "jang",
        inputdiary = diaryData,
        inputCreated_at = date.toString(),
        input_mainIMGpath  = "",
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

            IconButton(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 80.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }

            Text(
                text = "오늘의 일기",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray,
                modifier = Modifier.weight(1F),

                )
            Text(
                text = date.toString(),
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 32.dp)
            )

        }

        HorizontalDivider(thickness = 3.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(20.dp))
        ImageUploadIcon(imageUri, launcher, imageUrl)
        Spacer(modifier = Modifier.height(20.dp))

//        Column {
//            imageList.chunked(3).forEach { rowImages ->
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    rowImages.forEach { imageItem ->
//                        val itemUrl =
//                            "https://${BuildConfig.API_URL}.supabase.co/storage/v1/object/public/album/jang/${imageItem.name}"
//                        Image(
//                            painter = rememberAsyncImagePainter(itemUrl),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
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
                                uploadFileToSupabase(context, "album", "jang/$date/main", uri)
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
                                and {
                                    eq(column = "user", value = "jang")
                                    eq(column = "diary", value = diaryData)
                                }
                            }
                        }

                        deleteFileFromSupabaseLaunchIO(
                            fileName = "jang/$date/main.jpg"
                        )
                    }
                    Toast.makeText(context, "$date 일기 삭제 완료", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }, modifier = Modifier.weight(1f)
            ) {
                Text(text = "삭제")
            }
            Spacer(modifier = Modifier.weight(0.28f))
        }

//        Row() {
//            IconButton(onClick = { TODO() }) {
//                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
//            }
//
//
//            IconButton(onClick = { TODO() }) {
//                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "forward")
//            }
//        }
    }
}


@Serializable
data class DiaryLog(
    val user: String,
    val diary: String,
    val created_at: String,
    val mainIMGpath: String,
)

fun createDiaryLog(
    inputUser: String,
    inputdiary: String,
    inputCreated_at: String,
    input_mainIMGpath: String,
): DiaryLog {
    return DiaryLog(
        user = inputUser,
        diary = inputdiary,
        created_at = inputCreated_at,
        mainIMGpath = input_mainIMGpath
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ImageUploadIcon(
    imageUri: Uri?,
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    imageUrl: String?,
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