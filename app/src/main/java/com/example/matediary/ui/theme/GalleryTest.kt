package com.example.matediary.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import getImageList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GalleryTest {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoAlbumScreen(navController: NavHostController, date: String) {

    var selectedDate by remember { mutableStateOf(date) }
    val context = LocalContext.current
    var imageUrlsList by remember { mutableStateOf(listOf<String>()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var uploadSelectImgUri by remember { mutableStateOf<Uri?>(null) }
    val bucketName = "album"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { uploadSelectImgUri = it }
        }
    )

    LaunchedEffect(selectedDate) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                imageUrlsList = emptyList()
                val imageLogs = getImageList(bucketName, "jang/$selectedDate/")
                val imageNames = imageLogs.map { it.name }
                val urls = mutableListOf<String>()
                imageNames.forEach { imageName ->
                    getFileUrlFromSupabase(bucketName, "jang/$selectedDate/$imageName") { url ->
                        urls.add(url)
                        imageUrlsList = urls.toList() // Update the list with new URLs
                    }
                }

            } catch (e: Exception) {
                println("Error fetching images: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Photo Album")

                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                showDatePickerDialog(
                                    context,
                                    selectedDate,
                                ) { date ->
                                    selectedDate = date
                                }
                            }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Pick Date"
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedDate, textAlign = TextAlign.Center)
                        }

                        HorizontalDivider(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .padding(end = 30.dp)

                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("calendar")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
//           TODO() //선택 된 이미지 업로드
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(16.dp)
        ) {
            items(imageUrlsList) { imageUrl ->
                PhotoItem(imageUrl, onClick = { selectedImageUrl = it })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        selectedImageUrl?.let { url ->
            EnlargedImageView(imageUrl = url, onClose = { selectedImageUrl = null })
        }
    }

}


@Composable
fun PhotoItem(imageUrl: String, onClick: (String) -> Unit) {
    Image(
        painter = rememberAsyncImagePainter(model = imageUrl),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable {
                onClick(imageUrl)
            },
        contentScale = ContentScale.Crop
    )
}

fun showDatePickerDialog(context: Context, selectedDate: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()

    // 기존에 선택된 날짜가 있는 경우 해당 날짜로 설정
    if (selectedDate.isNotEmpty()) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        calendar.time = sdf.parse(selectedDate) ?: Date()
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            onDateSelected(selectedDate)
        },
        year,
        month,
        day
    ).show()
}

@Composable
fun EnlargedImageView(
    imageUrl: String,
    onClose: () -> Unit,
) {
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

//        Button(onClick = {
//            deleteFileFromSuperbaseLaunchIO(fileName)
//
//        }) {
//            Text(text = "삭제")
//        }
    }
}

