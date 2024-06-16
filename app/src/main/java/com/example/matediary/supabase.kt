import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.matediary.BuildConfig
import com.example.matediary.DiaryLog
import com.example.matediary.MateInfo
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.BucketItem
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.minutes


object SupabaseClient {
    val client get() = supabase

    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.API_URL,
        supabaseKey = BuildConfig.API_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }


    fun deleteUserLaunchIO(
        columnName: String,
        name: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            client.from("album").delete {
                filter {
                    eq(columnName, name)
                }
            }
        }
    }

    fun deleteFileFromSupabaseLaunchIO(fileName: String){
        val storage = supabase.storage
        CoroutineScope(Dispatchers.IO).launch {
            storage["album"].delete(fileName)
        }
    }
    fun getFileUrl(
        bucketName: String,
        fileName: String,
        onUrlRetrieved: (String) -> Unit,
    ) {
        val supabase = client
        val storage = supabase.storage

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = storage[bucketName].createSignedUrl(
                    fileName,
                    expiresIn = 3.minutes
                ) // URL 만료 시간 설정
                withContext(Dispatchers.Main) {
                    onUrlRetrieved(url)
                }
            } catch (e: Exception) {
                // 이미지가 없는 경우
            }
        }
    }
}

fun getFileUrlFromSupabase(
    bucketName: String,
    fileName: String,
    onUrlRetrieved: (String) -> Unit,
) {
    return SupabaseClient.getFileUrl(bucketName, fileName, onUrlRetrieved)
}

fun uploadFileToSupabase(context: Context, bucketName: String, fileName: String, imageUri: Uri) {
    val supabase = SupabaseClient.client
    val storage = supabase.storage

    CoroutineScope(Dispatchers.IO).launch {
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                // 새로운 파일 업로드
                storage[bucketName].upload("$fileName.jpg", bytes, true)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "이미지가 성공적으로 업로드되었습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "$bucketName ,  $fileName , $imageUri ", Toast.LENGTH_SHORT)
                Toast.makeText(context, "이미지 업로드 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                val errorMessage = "이미지 업로드 중 오류가 발생했습니다: ${e.message}"
                copyToClipboard(context,errorMessage)
            }
        }
    }
}


data class ImageItem(val name: String, var url: String? = null)

suspend fun getImageList(
    bucketName: String = "album",
    folderPath: String = "jang/2024-6-1/",
): List<BucketItem> {
    val bucket = SupabaseClient.client.storage.from(bucketName)
    return bucket.list(folderPath)
}

suspend fun getMateData(): List<MateInfo> {
    val supabase = SupabaseClient.client
    val result = supabase.from("mateinfo")
        .select()
        {
            filter {
                eq("user", "jang")
            }
        }.decodeAs<List<MateInfo>>()
    return result
}


//양식 : 2024-6-1
suspend fun DiaryGetData(date: String?): List<DiaryLog> {
    val supabase = SupabaseClient.client

    // yyyyMMdd 형식으로 변환 및 다음 날짜 계산
    val (formattedDate, nextDate) = date?.let {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 기존 형식
        val outputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault()) // 원하는 형식
        val parsedDate = inputFormat.parse(it)
        val formattedDate = parsedDate?.let { it1 -> outputFormat.format(it1) }

        val calendar = Calendar.getInstance().apply {
            time = parsedDate!!
            add(Calendar.DATE, 1)
        }
        val nextDate = outputFormat.format(calendar.time)
        formattedDate to nextDate
    } ?: (null to null)

    val result = supabase.from("DailyLog")
        .select()
        {
            filter {
                and {
                    eq("user", "jang")
                    DiaryLog::created_at eq formattedDate
                    DiaryLog::created_at lt nextDate
                }
            }
        }.decodeAs<List<DiaryLog>>()

    return result
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}