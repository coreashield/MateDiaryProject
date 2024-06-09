import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.matediary.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

fun createSupabaseClient(): SupabaseClient {
    return io.github.jan.supabase.createSupabaseClient(
        supabaseUrl = BuildConfig.API_URL,
        supabaseKey = BuildConfig.API_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}

fun getFileUrlFromSupabase(
    bucketName: String,
    fileName: String,
    onUrlRetrieved: (String) -> Unit
) {
    val supabase = createSupabaseClient()
    val storage = supabase.storage

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = storage[bucketName].createSignedUrl(
                fileName,
                expiresIn = 3.minutes
            ) // URL 만료 시간 설정 (예: 1일)
            withContext(Dispatchers.Main) {
                onUrlRetrieved(url)
            }
        } catch (e: Exception) {
            // 이미지가 없는 경우
        }
    }
}

fun uploadFileToSupabase(context: Context, bucketName: String, fileName: String, imageUri: Uri) {
    val supabase = createSupabaseClient()
    val storage = supabase.storage
    val bucket = storage.from("album")
    val uniqueFileName = "${System.currentTimeMillis()}"
    val saveFilename = "$fileName.jpg"
    val changeFilename = "${fileName}$uniqueFileName.jpg"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            if (saveFilename.contains("main.jpg")) {
                // 일기 작성할 때 해당 날짜의 대표 이미지. 이전 대표 이미지는 이름만 변경
                bucket.move(from = saveFilename, to = changeFilename)
            } else {
                // 대표이미지 변경할 때 사용, 이전 이미지는 삭제
                storage[bucketName].delete(saveFilename)
            }

            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                // 새로운 파일 업로드
                storage[bucketName].upload("$fileName.jpg", bytes, true)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "이미지가 성공적으로 업로드되었습니다!", Toast.LENGTH_SHORT).show()
//                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "이미지 업로드 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
data class ImageItem(val name: String, var url: String? = null)

suspend fun getImageList(
    bucketName: String = "album",
    folderPath: String = "jang/2024-06-01"
): List<ImageItem> {
    return withContext(Dispatchers.IO) {
        val bucket = createSupabaseClient().storage.from(bucketName)
        val files = bucket.list(folderPath)
        files.map { ImageItem(it.name) }
    }
}

