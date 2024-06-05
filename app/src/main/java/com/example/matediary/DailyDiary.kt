package com.example.matediary

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.matediary.ui.theme.MateDiaryTheme

class DailyDiary : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MateDiaryTheme {
                Column {
                    DiaryScreen()
                }
            }
        }
    }
}

@Composable
fun DiaryScreen(){
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var diaryData by remember {
        mutableStateOf("")
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // scope function  - let apply also run
            uri?.let { imageUri = it }
        }
    )
    val screenHeight = LocalConfiguration.current.screenHeightDp
    Column(modifier = Modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()){
            Text(text = "<-",modifier = Modifier
//                .align(alignment = Alignment.TopStart)
                ,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold)
            Text(text = "오늘의 일기", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Divider(color = Color.Green, thickness = 3.dp)
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .weight(1F)){
            MainImaeSelect(imageUri, launcher)
            OutlinedTextField(
                value = diaryData,
                onValueChange = {diaryData = it},
                modifier = Modifier.height(500.dp))

        }

    }
}
@Preview(showBackground = true)
@Composable
fun MainImaeSelectPreview() {

    DiaryScreen()
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
            .width(halfScreenWidth.dp)
            .height(imgMaxHeight.dp)
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
                .width((halfScreenWidth - 30).dp)
                .height((imgMaxHeight - 60).dp)
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                .clip(RoundedCornerShape(0.dp))
                .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 합니다.
            contentScale = ContentScale.Crop
        )

    }
}


