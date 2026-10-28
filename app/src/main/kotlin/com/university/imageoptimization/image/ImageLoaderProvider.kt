package com.university.imageoptimization.image

import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.university.imageoptimization.telemetry.PerformanceTracker
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Thông tin dung lượng thực tế của các tầng Cache đang hoạt động.
 */
data class CacheStorageStats(
    val memoryUsedKb: Long,
    val memoryMaxKb: Long,
    val diskUsedKb: Long,
    val diskMaxKb: Long
)

/**
 * Mô-đun quản lý ImageLoader và bộ nhớ đệm 3 tầng (Deliverable D4):
 * - Cung cấp 2 cấu hình đối lập rõ rệt: [createBaselineLoader] và [createOptimizedLoader].
 * - Tích hợp [NetworkSimulationInterceptor] cho phép:
 *   + Giả lập mất mạng hoàn toàn (`TC05_OFFLINE_MODE`) mà không cần bật/tắt Wi-Fi thật của máy.
 *   + Giả lập máy chủ phản hồi chậm (`TC06_ERROR_AND_SLOW`) với tham số `sim_delay_ms`.
 *   + Kiểm soát HTTP Cache độc lập với App Cache (Lưu ý trang 6 của đề bài).
 */
class ImageLoaderProvider(
    context: Context,
    private val performanceTracker: PerformanceTracker
) {
    private val appContext = context.applicationContext

    @Volatile
    var isSimulatedOffline: Boolean = false

    private val optimizedDiskCacheDir = File(appContext.cacheDir, "coil_optimized_disk_cache")
    private val okHttpCacheDir = File(appContext.cacheDir, "okhttp_http_cache")

    private val optimizedDiskCache: DiskCache by lazy {
        DiskCache.Builder()
            .directory(optimizedDiskCacheDir)
            .maxSizeBytes(150L * 1024L * 1024L) // 150 MB L2 Disk Cache
            .build()
    }

    /**
     * OkHttp Interceptor phục vụ kiểm soát điều kiện thực nghiệm (Chủ đề 10):
     * 1. Nếu đang bật chế độ Offline (TC05) -> ném IOException ngay ở tầng mạng (nhưng L1 RAM & L2 Disk của Coil vẫn hoạt động trước đó!).
     * 2. Nếu URL có tham số `sim_delay_ms` (TC06) -> sleep trên luồng mạng nền (IO thread) để kiểm chứng Main Thread không bị treo.
     */
    private val simulationInterceptor = Interceptor { chain ->
        val request = chain.request()
        if (isSimulatedOffline) {
            throw IOException("Simulated Offline Mode (TC05): Mạng đã bị ngắt có kiểm soát")
        }

        val delayParam = request.url.queryParameter("sim_delay_ms")?.toLongOrNull()
        if (delayParam != null && delayParam > 0L) {
            try {
                Thread.sleep(delayParam.coerceAtMost(5000L))
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Request cancelled during simulated slow server delay")
            }
        }

        val response: Response = chain.proceed(request)
        response
    }

    /**
     * Phiên bản Cơ sở chưa tối ưu (BASELINE):
     * - Tắt Memory Cache và Disk Cache (mọi lần cuộn qua hoặc mở lại đều phải tải và giải mã lại).
     * - Giải mã ảnh bằng cấu hình [Bitmap.Config.ARGB_8888] (4 bytes/pixel) trên Heap thường (`allowHardware(false)`).
     * - Không giới hạn số luồng giải mã song song hợp lý, dễ gây nghẽn GC.
     */
    val baselineImageLoader: ImageLoader by lazy {
        val baselineOkHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(simulationInterceptor)
            .build()

        ImageLoader.Builder(appContext)
            .okHttpClient(baselineOkHttp)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(false)
            .allowRgb565(false)
            .crossfade(false)
            .eventListenerFactory(performanceTracker.eventListenerFactory)
            .build()
    }

    /**
     * Phiên bản Đã tối ưu (OPTIMIZED):
     * - Tầng L1 (Memory Cache): Sử dụng 25% bộ nhớ ứng dụng cho `StrongMemoryCache` (LruCache)
     *   kết hợp `WeakMemoryCache` để giữ tham chiếu các ảnh vừa bị đẩy khỏi LruCache.
     * - Tầng L2 (Disk Cache): Lưu trữ 150 MB trên bộ nhớ flash nội bộ, tồn tại qua các lần khởi động lại ứng dụng và khi mất mạng (TC02, TC05).
     * - Tầng L3 (HTTP Cache): OkHttp Cache 50 MB hỗ trợ `Cache-Control` / `ETag`.
     * - Tối ưu giải mã & hiển thị:
     *   + Bật `allowHardware(true)` để đẩy Bitmap bất biến sang bộ nhớ GPU (giảm tải Java Heap).
     *   + Bật `allowRgb565(true)` cho ảnh không có kênh trong suốt (tiết kiệm 50% RAM: 2 bytes/px thay vì 4 bytes/px).
     *   + Sử dụng `Dispatchers.IO` giới hạn song song cho Fetch/Decode.
     */
    val optimizedImageLoader: ImageLoader by lazy {
        val httpCache = Cache(okHttpCacheDir, 50L * 1024L * 1024L)
        val optimizedOkHttp = OkHttpClient.Builder()
            .cache(httpCache)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .addInterceptor(simulationInterceptor)
            .build()

        ImageLoader.Builder(appContext)
            .okHttpClient(optimizedOkHttp)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.25) // 25% RAM khả dụng của tiến trình
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCache { optimizedDiskCache }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .allowHardware(true)
            .allowRgb565(true)
            .fetcherDispatcher(Dispatchers.IO)
            .decoderDispatcher(Dispatchers.IO.limitedParallelism(4))
            .crossfade(150)
            .respectCacheHeaders(false) // Đảm bảo DiskCache của Coil luôn hoạt động nhất quán khi kiểm thử
            .eventListenerFactory(performanceTracker.eventListenerFactory)
            .build()
    }

    fun getLoader(mode: OptimizationMode): ImageLoader {
        return when (mode) {
            OptimizationMode.BASELINE -> baselineImageLoader
            OptimizationMode.OPTIMIZED -> optimizedImageLoader
        }
    }

    /**
     * Xóa sạch L1 Memory Cache của cả 2 ImageLoader (Phục vụ đo Cold/Warm Disk Load).
     */
    fun clearMemoryCache() {
        baselineImageLoader.memoryCache?.clear()
        optimizedImageLoader.memoryCache?.clear()
        performanceTracker.appendLog("CACHE: Đã xóa sạch L1 Memory Cache (RAM)")
    }

    /**
     * Xóa sạch cả L1 Memory Cache và L2 Disk Cache (Phục vụ kịch bản TC01 Cold Load).
     */
    @OptIn(ExperimentalCoilApi::class)
    fun clearAllCaches() {
        baselineImageLoader.memoryCache?.clear()
        optimizedImageLoader.memoryCache?.clear()
        try {
            optimizedDiskCache.clear()
        } catch (_: Exception) {
        }
        performanceTracker.appendLog("CACHE: Đã xóa sạch cả L1 RAM Cache & L2 Disk Cache")
    }

    @OptIn(ExperimentalCoilApi::class)
    fun getCacheStats(): CacheStorageStats {
        val memCache = optimizedImageLoader.memoryCache
        val memUsedKb = (memCache?.size ?: 0) / 1024L
        val memMaxKb = (memCache?.maxSize ?: 0) / 1024L

        val diskUsedKb = try {
            optimizedDiskCache.size / 1024L
        } catch (_: Exception) {
            0L
        }
        val diskMaxKb = try {
            optimizedDiskCache.maxSize / 1024L
        } catch (_: Exception) {
            150L * 1024L
        }

        return CacheStorageStats(
            memoryUsedKb = memUsedKb,
            memoryMaxKb = memMaxKb,
            diskUsedKb = diskUsedKb,
            diskMaxKb = diskMaxKb
        )
    }
}
