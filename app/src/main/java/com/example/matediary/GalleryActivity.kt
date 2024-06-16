package com.example.matediary

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Button
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import getImageList
import io.github.jan.supabase.storage.BucketItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uploadFileToSupabase
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class GalleryActivity() : ComponentActivity() {}

@Composable
fun GalleryView(
    date: String,
    navController: NavHostController,
) {
    var imageLogs by remember { mutableStateOf<List<BucketItem>>(listOf()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val userName = "jang"
    val context = LocalContext.current

    // selectedDate 변수를 remember로 상태로 저장합니다.
    var selectedDate by remember { mutableStateOf(date) }

    // 현재 시간으로 이미지 저장
    val currentTime = System.currentTimeMillis()
    val fileName = "$userName/$selectedDate/$currentTime"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val imageUrlsList = remember {
            mutableStateListOf<String?>()
        }

        // selectedDate가 변경될 때 이미지를 다시 가져오기
        LaunchedEffect(selectedDate) {
            imageUrlsList.clear() // 이미지 목록 초기화
            CoroutineScope(Dispatchers.IO).launch {
                imageLogs = getImageList(bucketName = "album", folderPath = "jang/$selectedDate/")
                val imageNames = imageLogs.map { it.name }
                imageNames.forEach {
                    getFileUrlFromSupabase("album", "jang/$selectedDate/$it") { url ->
                        imageUrlsList.add(url)
                    }
                }
            }
        }
        Text(text = selectedDate)

        // HeaderMenu를 호출할 때 selectedDate를 전달합니다.
        HeaderMenu(selectedDate, navController) { newDate ->
            selectedDate = newDate
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Box {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    items(imageUrlsList.chunked(3)) { rowImages ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            rowImages.forEach { imageUrl ->
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(104.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            selectedImageUrl = imageUrl
                                        },
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                UploadPictureButton("album", fileName, context, onImageUploaded = {
                    selectedDate = date // Recompose screen by updating selectedDate
                })
            }
        }
    }
}

@Composable
fun HeaderMenu(date: String, navController: NavHostController, onDateSelected: (String) -> Unit) {
    val showDatePicker = remember { mutableStateOf(false) }

    // 날짜 선택기가 표시되어야 하는지 여부를 결정하는 상태
    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            onConfirm = {} // OK 버튼이 눌렸을 때 별도 동작 없음, onDateSelected 콜백에서 처리
        ) {
            DatePickerWithDateSelectableDatesSample { selectedDate ->
                onDateSelected(selectedDate)
                showDatePicker.value = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = { navController.navigate("calendar") }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                Modifier
                    .padding(start = 4.dp),
            )
        }
        Text(
            text = "뒤로",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 10.dp)
        )

        Icon(
            Icons.Rounded.DateRange,
            contentDescription = "CalenderIcon",
            modifier = Modifier
                .padding(start = 64.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    showDatePicker.value = true
                }
        )

        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .padding(end = 72.dp)
                .clickable {
                    showDatePicker.value = true
                }
        )
    }
}

@Composable
fun UploadPictureButton(
    bucketname: String,
    filename: String,
    context: Context,
    onImageUploaded: () -> Unit,
) {
    var photopickerImgUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { photopickerImgUri = it }
        }
    )

    SmallFloatingActionButton(
        onClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        containerColor = MaterialTheme.colors.secondary,
    ) {
        Icon(
            imageVector = Icons.Rounded.AddCircle,
            contentDescription = "Add FAB",
            tint = Color.White,
        )
    }
    photopickerImgUri?.let { uploadFileToSupabase(context, bucketname, filename, it) }
    photopickerImgUri = null
    onImageUploaded()
}

@Composable
fun DatePickerDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Select Date")
        },
        text = {
            content()
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerWithDateSelectableDatesSample(onDateSelected: (String) -> Unit) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            // 일요일과 토요일 선택 불가
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dayOfWeek =
                        Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneId.of("Asia/Seoul")) // 한국 시간대 적용
                            .toLocalDate()
                            .dayOfWeek
                    dayOfWeek != DayOfWeek.SUNDAY && dayOfWeek != DayOfWeek.SATURDAY
                } else {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")) // 한국 시간대 적용
                    calendar.timeInMillis = utcTimeMillis
                    calendar[Calendar.DAY_OF_WEEK] != Calendar.SUNDAY &&
                            calendar[Calendar.DAY_OF_WEEK] != Calendar.SATURDAY
                }
            }

            // 2023년 이후의 날짜만 선택 가능
            override fun isSelectableYear(year: Int): Boolean {
                return year > 2022
            }
        }
    )

    val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
            calendar.timeInMillis = millis
            SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(calendar.time)
        }
    } ?: "no selection"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePicker(state = datePickerState)
        Text(
            "Selected date: $selectedDate",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    onDateSelected(selectedDate)
                }
        )
    }
}
