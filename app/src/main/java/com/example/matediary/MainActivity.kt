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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.matediary.ui.theme.MateDiaryTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            MateDiaryTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
//    CountriesList()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if(uri != null){
                imageUri = uri
            }
        }
    )

    Column(modifier = Modifier
        .background(color = Color.White)
        .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "정보" , fontSize = 30.sp, fontWeight = FontWeight.Bold)

        PhotoPickerScreen(imageUri,launcher)

        EditInfo()
        SubmitBtn()
    }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}

@Serializable
data class Country(
    val id: Int,
    val name: String,
    val count: String,
)

@Composable
fun CountriesList() {
    val supabase = createSupabaseClient(
        supabaseUrl = "",
        supabaseKey = ""
    ) {
        install(Postgrest)
    }


    var countries by remember { mutableStateOf<List<Country>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            countries = supabase.from("countries")
                .select().decodeList<Country>()
        }
    }

    LazyColumn {
        items(
            countries,
            key = { country -> country.id },
        ) { country ->
            Row {
                Text(country.name, modifier = Modifier.padding(8.dp))
                Text(country.count, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable

fun PhotoPickerScreen(imageUri: Uri?, launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>) {
    Box {
        //변수 하나로 틀과 이미지 크기 동시 관리
        var ImgMaxHegight = 320
        // 기존 이미지
        Image(
            painter = painterResource(id = R.drawable.upload_img),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
//                .height(ImgMaxHegight.dp)
                .clip(RoundedCornerShape(0.dp))
                .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentScale = ContentScale.Fit
        )

        // 선택된 이미지 (겹치는 이미지)
        Image(
            painter = rememberImagePainter(imageUri), // selectedImagePainter는 선택된 이미지를 로드하는 Painter여야 합니다.
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(ImgMaxHegight.dp)
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                .clip(RoundedCornerShape(0.dp))
                .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 합니다.
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EditInfo() {
    // 반려 파트너 종류와 이름을 저장할 변수들
    val petTypeState = remember { mutableStateOf("") }
    val nameState = remember { mutableStateOf("") }

    Column {
        // 반려 파트너 종류를 입력하는 OutlinedTextField
        OutlinedTextField(
            value = petTypeState.value,
            onValueChange = { petTypeState.value = it },
            label = { Text("반려 파트너 종류") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 이름을 입력하는 OutlinedTextField
        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text("이름") },
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }
}

@Composable
fun SubmitBtn(){
    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Button(onClick = { /*TODO*/ }) {
            Text(text = "등록")
        }

        Button(onClick = { /*TODO*/ }) {
            Text(text = "삭제")
        }
    }
}