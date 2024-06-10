package com.example.matediary

import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import getImageList
import io.github.jan.supabase.storage.BucketItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class GalleryActivity() : ComponentActivity() {}

@Composable
fun GalleryView(date: String, navController: NavHostController) {
    var imageLogs by remember { mutableStateOf<List<BucketItem>>(listOf()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val yearState = remember { mutableStateOf("") }
    val monthState = remember { mutableStateOf("") }
    val dayState = remember { mutableStateOf("") }
    val context = LocalContext.current
    val datePickerDialog = createDatePickerDialog(context, yearState, monthState, dayState)
    var selectedDate by remember { mutableStateOf(date) }

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

        // 날짜 상태가 변경될 때 selectedDate를 업데이트
        LaunchedEffect(yearState.value, monthState.value, dayState.value) {
            if (yearState.value.isNotEmpty() && monthState.value.isNotEmpty() && dayState.value.isNotEmpty()) {
                selectedDate = "${yearState.value}-${monthState.value}-${dayState.value}"
            }
        }

//        Text(text = selectedDate)
        HeaderMenu(selectedDate, navController,datePickerDialog)
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                                    .padding(4.dp)
                                    .size(128.dp)
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
    }

    // 선택된 이미지가 있을 때 EnlargedImageView를 표시합니다.
    selectedImageUrl?.let { imageUrl ->
        EnlargedImageView(
            imageUrl = imageUrl,
            onClose = { selectedImageUrl = null }
        )
    }
}

@Composable
fun EnlargedImageView(imageUrl: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClose), // 이미지를 클릭하면 닫기 동작 실행
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun HeaderMenu(date: String, navController: NavHostController,datePickerDialog: DatePickerDialog) {
    var selectedDate by remember { mutableStateOf(date) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(4.dp),
//        verticalAlignment = Alignment.CenterVertically
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = { navController.navigate("calendar") }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                Modifier
                    .padding(start = 8.dp),
            )
        }
        Text(
            text = "뒤로",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )

        Icon(
            Icons.Rounded.DateRange,
            contentDescription = "CalenderIcon",
            modifier = Modifier
                .padding(start = 80.dp)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = date,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 96.dp)
                .clickable {
                    datePickerDialog.show()
                }
        )

    }
}
