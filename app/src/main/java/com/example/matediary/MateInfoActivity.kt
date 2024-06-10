package com.example.matediary

import SupabseClient
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import getData
import getFileUrlFromSupabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uploadFileToSupabase
import java.util.Calendar

class MainActivity : ComponentActivity() {}

@Composable
fun MainScreen(navController: NavHostController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { imageUri = it }
        }
    )
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // 파일 URL 가져오기
    LaunchedEffect(Unit) {
        getFileUrlFromSupabase("infoImg", "jang/infoImg.jpg") { url ->
            imageUrl = url
        }
    }

    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically // 수직 가운데 정렬
        ) {
            IconButton(onClick = { navController.navigate("calendar") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }
            Spacer(modifier = Modifier.weight(1f)) // "정보" 왼쪽 여백
            Text(
                text = "정보",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1.6f)) // "정보" 오른쪽 여백
        }

        PhotoPickerScreen(imageUri, launcher, imageUrl)
        EditInfo(imageUri, setImageUri = { imageUri = it })
    }
}

@Composable
fun PhotoPickerScreen(
    imageUri: Uri?,
    launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    imageUrl: String?,
) {
    Box {
        // 변수 하나로 틀과 이미지 크기 동시 관리
        val imgMaxHeight = 240

        // 클릭할 수 있는 기본 이미지
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

        // 선택된 이미지 또는 불러온 이미지 (겹치는 이미지)
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .width((imgMaxHeight - 30).dp)
                    .height((imgMaxHeight - 30).dp)
                    .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 합니다.
                contentScale = ContentScale.Crop
            )
        } else if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .width((imgMaxHeight - 30).dp)
                    .height((imgMaxHeight - 30).dp)
                    .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .align(Alignment.Center), // 이미지를 가운데 정렬하여 겹치게 합니다.
                contentScale = ContentScale.Crop
            )
        }
    }
}


//SAVE
@Composable
fun EditInfo(imageUri: Uri?, setImageUri: (Uri?) -> Unit) {
    val petTypeState = remember { mutableStateOf("") }
    var nameState = remember { mutableStateOf("") }
    val yearState = remember { mutableStateOf("") }
    val monthState = remember { mutableStateOf("") }
    val dayState = remember { mutableStateOf("") }
    val context = LocalContext.current
    val datePickerDialog = createDatePickerDialog(context, yearState, monthState, dayState)


    //1번만 이용
    LaunchedEffect(key1 = Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            getData().takeIf { it.isNotEmpty() }?.let {
                petTypeState.value = it[0].type
                nameState.value = it[0].name
                yearState.value = it[0].year
                monthState.value = it[0].month
                dayState.value = it[0].day
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        PetTypeInput(petTypeState)
        Spacer(modifier = Modifier.height(8.dp))
        NameInput(nameState)
        DatePickerButton(datePickerDialog)
        DateInputs(yearState, monthState, dayState)
    }

    SubmitBtn(
        context,
        petTypeState,
        nameState,
        yearState,
        monthState,
        dayState,
        imageUri,
        setImageUri
    )
}

fun createDatePickerDialog(
    context: Context,
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            yearState.value = selectedYear.toString()
            monthState.value = (selectedMonth + 1).toString()
            dayState.value = selectedDay.toString()
        },
        year, month, day
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetTypeInput(petTypeState: MutableState<String>) {
    OutlinedTextField(
        value = petTypeState.value,
        onValueChange = { petTypeState.value = it },
        label = { Text("반려 파트너 종류") },
    )
}

@Composable
fun NameInput(nameState: MutableState<String>) {
    OutlinedTextField(
        value = nameState.value,
        onValueChange = { nameState.value = it },
        label = { Text("이름") },
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun DatePickerButton(datePickerDialog: DatePickerDialog) {
    Button(onClick = { datePickerDialog.show() }) {
        Text(text = "태어난 날")
    }
}

@Composable
fun DateInputs(
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
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

@Serializable
data class MateInfo(
    val user: String,
    val type: String,
    val name: String,
    val year: String,
    val month: String,
    val day: String,
)

fun createMateInfo(
    petTypeState: MutableState<String>,
    nameState: MutableState<String>,
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
): MateInfo {
    return MateInfo(
        user = "jang",
        type = petTypeState.value,
        name = nameState.value,
        year = yearState.value,
        month = monthState.value,
        day = dayState.value
    )
}

@Composable
fun SubmitBtn(
    context: Context,
    petTypeState: MutableState<String>,
    nameState: MutableState<String>,
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
    imageUri: Uri?,
    setImageUri: (Uri?) -> Unit,
) {
    val insertInfo = createMateInfo(petTypeState, nameState, yearState, monthState, dayState)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RegisterButton(
            petTypeState,
            nameState,
            yearState,
            monthState,
            dayState,
            insertInfo,
            imageUri,
        )
        Spacer(modifier = Modifier.width(16.dp))
        DeleteButton(
            resetScreenData = {
                petTypeState.value = ""
                nameState.value = ""
                yearState.value = ""
                monthState.value = ""
                dayState.value = ""
                setImageUri(null)
            },
            nameState,
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun RegisterButton(
    petTypeState: MutableState<String>,
    nameState: MutableState<String>,
    yearState: MutableState<String>,
    monthState: MutableState<String>,
    dayState: MutableState<String>,
    insertInfo: MateInfo,
    imageUri: Uri?,
) {
    val context = LocalContext.current
    val supabase = SupabseClient.client
    Button(onClick = {
        if (petTypeState.value.isEmpty() || nameState.value.isEmpty() ||
            yearState.value.isEmpty() || monthState.value.isEmpty() || dayState.value.isEmpty()
        ) {
            Toast.makeText(context, "정보를 전부 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                //TODO: check upsert

                val checkData = supabase.from("mateinfo")
                    .select {
                        filter {
                            eq("user", "jang")
                        }
                    }.decodeAs<List<MateInfo>>()

                if (checkData.isEmpty()) {
                    // 레코드가 존재하지 않으므로 새로운 레코드 삽입
                    supabase.from("mateinfo").insert(insertInfo)
                } else {
                    // 이미 레코드가 존재하므로 업데이트 수행
                    supabase.from("mateinfo").update(insertInfo) {
                        filter {
                            eq("user", "jang")
                        }
                    }
                }
            }

            if (imageUri != null) {
                uploadFileToSupabase(context, "infoImg", "jang/infoImg", imageUri)
            } else {
//                Toast.makeText(context, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(context, "${nameState.value} 정보가 저장되었어요!", Toast.LENGTH_SHORT).show()
        }
    }) {
        Text(text = "등록")
    }
}

@Composable
fun DeleteButton(
    resetScreenData: () -> Unit,
    nameState: MutableState<String>,
) {
    val context = LocalContext.current
    Button(onClick = {
        SupabseClient.deleteUserLaunchIO("jang")
        resetScreenData()
        Toast.makeText(context, "${nameState.value} 정보가 삭제되었어요ㅜㅜ 안녕", Toast.LENGTH_SHORT).show()
    }) {
        Text(text = "삭제")
    }
}

