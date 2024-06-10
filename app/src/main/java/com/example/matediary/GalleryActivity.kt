package com.example.matediary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getFileUrlFromSupabase
import getImageList
import io.github.jan.supabase.storage.BucketItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryActivity() : ComponentActivity() {}

@Composable
fun GalleryView(date: String, navController: NavHostController) {
    var imageLogs by remember { mutableStateOf<List<BucketItem>>(listOf()) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier.height(
                32.dp
            )
        )
        val imageUrlsList = remember {
            mutableStateListOf<String?>()
        }
        Text("날짜:$date")
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                imageLogs = getImageList(bucketName = "album", folderPath = "jang/$date/")
                val imageNames = imageLogs.map { it.name }
                imageNames.forEach {
                    getFileUrlFromSupabase("album", "jang/$date/$it") { url ->
                        imageUrlsList.add(url)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            content = {
                items(imageUrlsList.chunked(2)) { rowImages ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (rowImages.size == 1) Arrangement.Start else Arrangement.Center
                    ) {
                        rowImages.forEach { imageUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = if (rowImages.size == 1) 50.dp else 4.dp) // 조건 추가
                                    .size(150.dp)
                                    .clickable {
                                        selectedImageUrl = imageUrl
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        )

    }

// 선택된 이미지가 있을 때 EnlargedImageView를 표시합니다.
    selectedImageUrl?.let { imageUrl ->
        EnlargedImageView(
            imageUrl = imageUrl,
            onClose = { selectedImageUrl = null }
            // 닫기 버튼을 클릭하면 selectedImageUrl을 null로 설정하여 EnlargedImageView를 닫습니다.
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