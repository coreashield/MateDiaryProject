package com.example.matediary

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.matediary.ui.theme.MateDiaryTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


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
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
            }
        }
    )

    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "정보", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        PhotoPickerScreen(imageUri, launcher)

        EditInfo(imageUri, setImageUri = { imageUri = it })
    }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}


@Composable
fun PhotoPickerScreen(
    imageUri: Uri?,
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {
    Box {
        //변수 하나로 틀과 이미지 크기 동시 관리
        var ImgMaxHegight = 240
        // 기존 이미지
        Image(
            painter = painterResource(id = R.drawable.upload_img),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(ImgMaxHegight.dp)
                .clip(RoundedCornerShape(0.dp))
                .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentScale = ContentScale.Fit
        )

        // 선택된 이미지 (겹치는 이미지)
        Image(
            painter = rememberImagePainter(imageUri), // selectedImagePainter는 선택된 이미지를 로드하는 Painter여야 합니다.
            contentDescription = null,
            modifier = Modifier
                .width((ImgMaxHegight - 30).dp)
                .height((ImgMaxHegight - 30).dp)
//                .height((ImgMaxHegight - 20).dp)
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                .clip(RoundedCornerShape(0.dp))
                .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 합니다.
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInfo(imageUri: Uri?, setImageUri: (Uri?) -> Unit) {
    val petTypeState = remember { mutableStateOf("") }
    val nameState = remember { mutableStateOf("") }
    val yearState = remember { mutableStateOf("") }
    val monthState = remember { mutableStateOf("") }
    val dayState = remember { mutableStateOf("") }
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    var year = calendar.get(Calendar.YEAR)
    var month = calendar.get(Calendar.MONTH)
    var day = calendar.get(Calendar.DAY_OF_MONTH)

    // DatePickerDialog 생성
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            yearState.value = selectedYear.toString()
            monthState.value = (selectedMonth + 1).toString()
            dayState.value = selectedDay.toString()
        }, year, month, day
    )


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = petTypeState.value,
            onValueChange = { petTypeState.value = it },
            label = { Text("반려 파트너 종류") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Blue,
                unfocusedBorderColor = Blue
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text("이름") },
            modifier = Modifier.padding(bottom = 20.dp)
        )


        Button(onClick = { datePickerDialog.show() }) {
            Text(text = "태어난 날")
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            // 선택된 년, 월, 일 표시
            OutlinedTextField(
                value = yearState.value,
                onValueChange = { yearState.value = it },
                label = { Text("년도") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = monthState.value,
                onValueChange = { monthState.value = it },
                label = { Text("월") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = dayState.value,
                onValueChange = { dayState.value = it },
                label = { Text("일") },
                modifier = Modifier.weight(1f)
            )
        }
    }

    SubmitBtn(context, petTypeState, nameState, yearState, monthState, dayState, setImageUri)

}

@Serializable
data class MateInfo(
    val type: String,
    val name: String,
    val year: String,
    val month: String,
    val day: String,
    val user: String,
)

@Composable
fun SubmitBtn(
    context: Context,
    petTypeState: MutableState<String>,
    nameState: MutableState<String>,
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
    setImageUri: (Uri?) -> Unit
) {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://rooxnjbwwvkgbwlfdnen.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJvb3huamJ3d3ZrZ2J3bGZkbmVuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTcxMjkyNjAsImV4cCI6MjAzMjcwNTI2MH0.b982U5hA2NqyXEoxqoEHoH18NQexwftWoXd0gHoObB4"
    ) {
        install(Auth)
        install(Postgrest)
    }

    val insertInfo = MateInfo(
        user = "jang",
        type = petTypeState.value,
        name = nameState.value,
        year = yearState.value,
        month = monthState.value,
        day = dayState.value
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            if (petTypeState.value.isEmpty() || nameState.value.isEmpty() ||
                yearState.value.isEmpty() || monthState.value.isEmpty() || dayState.value.isEmpty()
            ) {
                Toast.makeText(context, "정보를 전부 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
//            신규등록
                CoroutineScope(Dispatchers.IO).launch {
                    supabase.from("mateinfo").insert(insertInfo)
                }
                Toast.makeText(context, nameState.value + " 정보가 저장되었어요!", Toast.LENGTH_SHORT).show()
            }

        }) {
            Text(text = "등록")
        }

        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                supabase.from("mateinfo").delete {
                    filter {
                        eq("user", petTypeState.value)
                    }
                }
            }
            // 화면 초기화
            petTypeState.value = ""
            nameState.value = ""
            yearState.value = ""
            monthState.value = ""
            dayState.value = ""
            setImageUri(null)

            Toast.makeText(context, nameState.value + " 정보가 삭제되었어요ㅜㅜ 안녕", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "삭제")
        }
    }
}

