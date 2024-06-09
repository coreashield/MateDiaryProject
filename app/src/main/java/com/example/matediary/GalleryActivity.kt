package com.example.matediary

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class galleryActivity() : ComponentActivity() {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerView(navController: NavHostController) {
    val selectedDate = remember { mutableStateOf("") }
    val today = Calendar.getInstance()

    var imageUrl by remember { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())) {
        AndroidView(factory = { context ->
            android.widget.CalendarView(context).apply {
                date = today.timeInMillis
                setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val date = "$year-${month + 1}-$dayOfMonth"
                    selectedDate.value = date
                }
            }
        }, modifier = Modifier.padding(top = 20.dp))

        Spacer(
            modifier = Modifier.height(
                32.dp
            )
        )

        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val currentDate = selectedDate.value // 현재 선택된 날짜를 가져옴
                getFileUrlFromSupabase(
                    bucketName = "album",
                    fileName = "jang/$currentDate/main.jpg",
                ) { url ->
                    imageUrl = url
                }
            }
        }


        if(imageUrl != null){
            val imageCount = 9 // 이미지 수
            val rows = imageCount / 3 // 3으로 나눈 몫이 행 수
            for (rowIndex in 0 until rows) {
                Row {
                    for (columnIndex in 0 until 3) { // 한 행당 3개의 이미지를 표시
                        val index = rowIndex * 3 + columnIndex // 이미지 인덱스 계산
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .padding(8.dp), // 이미지 사이의 간격을 조절할 수 있습니다.
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }else{
            Text(text = "저장 된 이미지 없음")
        }
    }
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-M-d")
    return formatter.format(Date(millis))
}

