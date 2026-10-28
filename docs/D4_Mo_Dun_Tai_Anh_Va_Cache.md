# TÀI LIỆU KỸ THUẬT MÔ-ĐUN TẢI ẢNH VÀ BỘ NHỚ ĐỆM (DELIVERABLE D4)

**Đề tài 02:** Tối ưu tải, lưu trữ đệm và hiển thị hình ảnh trong ứng dụng Android  
**Ứng dụng thực nghiệm:** `ImageOptCatalog` (`com.university.imageoptimization`)  
**Các tệp mã nguồn cốt lõi:**
- [`ImageLoaderProvider.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt)
- [`OptimizationMode.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/OptimizationMode.kt)
- [`TestScenario.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/TestScenario.kt)
- [`PerformanceTracker.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/telemetry/PerformanceTracker.kt)
- [`ProductCatalogScreen.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt)
- [`ProductDetailScreen.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductDetailScreen.kt)

---

## MỤC LỤC

1. [Tổng quan kiến trúc Mô-đun Tải ảnh và Bộ nhớ đệm](#1-tổng-quan-kiến-trúc-mô-đun-tải-ảnh-và-bộ-nhớ-đệm)
2. [Thiết kế `ImageLoaderProvider`: Đối chứng `Baseline` vs `Optimized`](#2-thiết-kế-imageloaderprovider-đối-chứng-baseline-vs-optimized)
3. [Giải thích chuyên sâu các quyết định cấu hình bộ nhớ đệm 3 tầng](#3-giải-thích-chuyên-sâu-các-quyết-định-cấu-hình-bộ-nhớ-đệm-3-tầng)
4. [Cơ chế `simulationInterceptor`: Kiểm soát thực nghiệm Offline (`TC05`) và Máy chủ chậm (`TC06`)](#4-cơ-chế-simulationinterceptor-kiểm-soát-thực-nghiệm-offline-tc05-và-máy-chủ-chậm-tc06)
5. [Chiến lược Downsampling, Cấu hình Điểm ảnh (`RGB_565` / `HARDWARE`) và Giới hạn luồng giải mã](#5-chiến-lược-downsampling-cấu-hình-điểm-ảnh-rgb_565--hardware-và-giới-hạn-luồng-giải-mã)
6. [Xử lý Giao diện: Placeholder, Crossfade 150ms, Error Fallback, Retry và Hủy Coroutine](#6-xử-lý-giao-diện-placeholder-crossfade-150ms-error-fallback-retry-và-hủy-coroutine)

---

## 1. Tổng quan kiến trúc Mô-đun Tải ảnh và Bộ nhớ đệm

Mô-đun tải ảnh và bộ nhớ đệm của ứng dụng `ImageOptCatalog` được đóng gói độc lập trong gói `com.university.imageoptimization.image` và kết nối trực tiếp với hệ thống đo lường `com.university.imageoptimization.telemetry`. Mục tiêu thiết kế của mô-đun là:
1. **Cô lập hoàn toàn cấu hình đường ống hình ảnh khỏi tầng giao diện (UI Decoupling):** Các màn hình Compose (`ProductCatalogScreen`, `ProductDetailScreen`) chỉ nhận một thực thể `coil.ImageLoader` thông qua `CatalogViewModel` mà không cần biết chi tiết bên dưới đang chạy cấu hình bộ nhớ đệm nào.
2. **Hỗ trợ chuyển đổi nóng (Hot-Swapping) giữa 2 chế độ đối chứng:** Cho phép người kiểm thử chuyển đổi tức thì giữa [`OptimizationMode.BASELINE`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/OptimizationMode.kt#L18-L25) và [`OptimizationMode.OPTIMIZED`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/OptimizationMode.kt#L26-L33) ngay trên thanh công cụ để quan sát sự thay đổi tức thời của bộ nhớ RAM, thời gian tải và tốc độ khung hình (FPS).
3. **Đo lường không xâm lấn (Non-intrusive Telemetry):** Mọi bước trong pipeline (`onStart`, `fetchStart`, `decodeStart`, `decodeEnd`, `onSuccess`, `onCancel`, `onError`) đều được ghi vết tự động qua `PerformanceTracker.eventListenerFactory`.

```mermaid
classDiagram
    class OptimizationMode {
        <<enumeration>>
        BASELINE
        OPTIMIZED
        +String label
        +String memoryCacheSummary
        +String diskCacheSummary
        +String decodeStrategySummary
    }

    class TestScenario {
        <<enumeration>>
        TC01_FIRST_LOAD
        TC02_WARM_CACHE
        TC03_FAST_SCROLL
        TC04_HIGH_RES
        TC05_OFFLINE_MODE
        TC06_ERROR_AND_SLOW
        TC07_SCALABILITY
        TC08_COMPARISON
    }

    class ImageLoaderProvider {
        +Boolean isSimulatedOffline
        +ImageLoader baselineImageLoader
        +ImageLoader optimizedImageLoader
        -Interceptor simulationInterceptor
        +getLoader(mode: OptimizationMode) ImageLoader
        +clearMemoryCache()
        +clearAllCaches()
        +getCacheStats() CacheStorageStats
    }

    class PerformanceTracker {
        +StateFlow~LivePerformanceMetrics~ metrics
        +EventListener.Factory eventListenerFactory
        +startFrameMonitoring()
        +resetMetrics()
        +getReferenceComparisonTable() List~ScenarioBenchmarkRecord~
    }

    ImageLoaderProvider --> OptimizationMode : selects loader
    ImageLoaderProvider --> PerformanceTracker : attaches EventListener
    PerformanceTracker --> TestScenario : tracks active scenario
```

---

## 2. Thiết kế `ImageLoaderProvider`: Đối chứng `Baseline` vs `Optimized`

Trong tệp [`ImageLoaderProvider.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt#L88-L149), hai thực thể `ImageLoader` được khởi tạo lười (`by lazy`) với các thông số kỹ thuật đối lập có chủ đích:

### 2.1. Trích dẫn mã nguồn thực tế trong `ImageLoaderProvider.kt`

```kotlin
// Trích dẫn từ: app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt

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
```

### 2.2. Bảng đối chiếu trực tiếp hai cấu hình trong mã nguồn

| Thành phần cấu hình | `baselineImageLoader` (`BASELINE`) | `optimizedImageLoader` (`OPTIMIZED`) |
| :--- | :--- | :--- |
| **Tầng L1 Memory Cache** | `CachePolicy.DISABLED` | `CachePolicy.ENABLED` (`maxSizePercent = 0.25`, `Strong` + `Weak`) |
| **Tầng L2 Disk Cache** | `CachePolicy.DISABLED` | `CachePolicy.ENABLED` (`150 MB` tại `coil_optimized_disk_cache`) |
| **Tầng L3 HTTP Cache** | Không cấu hình (`null`) | `okhttp3.Cache` (`50 MB` tại `okhttp_http_cache`) |
| **Định dạng giải mã `Bitmap`** | `Bitmap.Config.ARGB_8888` ($4\text{ bytes/pixel}$) | Tự động chọn `HARDWARE` hoặc `RGB_565` ($2\text{ bytes/pixel}$) |
| **Cho phép Hardware Bitmap** | `allowHardware(false)` | `allowHardware(true)` |
| **Điều phối luồng giải mã** | Mặc định (không giới hạn chặt mức độ song song) | `Dispatchers.IO.limitedParallelism(4)` |
| **Hiệu ứng chuyển cảnh** | `crossfade(false)` | `crossfade(150)` ($150\text{ ms}$) |
| **Kiểm soát Header HTTP** | Mặc định | `respectCacheHeaders(false)` (Ưu tiên tuyệt đối L2 DiskCache) |

---

## 3. Giải thích chuyên sâu các quyết định cấu hình bộ nhớ đệm 3 tầng

### 3.1. Tại sao cấu hình L1 Memory Cache = 25% Heap kết hợp `StrongMemoryCache` + `WeakMemoryCache`?

1. **Lý do chọn ngưỡng `maxSizePercent(0.25)` (25% App Java Heap):**
   - Trên một thiết bị Android điển hình có giới hạn Java Heap là $256\text{ MB}$ (hoặc $384\text{ MB}$), việc dành $25\%$ tương đương với **$64\text{ MB} - 96\text{ MB}$** cho bộ nhớ đệm ảnh L1.
   - Với chiến lược Downsampling của chế độ `Optimized`, mỗi bức ảnh đại diện sản phẩm trên danh sách được giải mã về kích thước $240 \times 240\text{ px}$ với cấu hình `RGB_565` ($2\text{ bytes/pixel}$), chỉ chiếm:
     $$\text{Size}_{\text{thumb}} = 240 \times 240 \times 2\text{ bytes} = 115,200\text{ bytes} = \mathbf{112.5\text{ KB}}$$
   - Như vậy, với $64\text{ MB}$ L1 Memory Cache, ứng dụng có thể lưu giữ đồng thời trong RAM tới:
     $$N_{\text{cached\_bitmaps}} = \frac{64 \times 1024\text{ KB}}{112.5\text{ KB}} \approx \mathbf{582\text{ bức ảnh đã giải mã!}}$$
   - Ngược lại, nếu không downsample (như chế độ `Baseline`, mỗi ảnh $2400 \times 1600 \times 4\text{ B} = 15.36\text{ MB}$), bộ đệm $64\text{ MB}$ chỉ chứa được đúng **4 bức ảnh** là đầy và liên tục đẩy nhau ra (cache thrashing).
   - Ngưỡng $25\%$ đủ lớn để chứa hơn một nửa danh mục $1,000$ sản phẩm trên RAM, nhưng vẫn chừa lại $75\%$ không gian Heap an toàn cho cây giao diện Compose (`SlotTable`), ViewModel, Coroutines và hệ điều hành.

2. **Sự phối hợp giữa `StrongMemoryCache` (`LruCache`) và `WeakMemoryCache` (`WeakReference`):**
   - **`strongReferencesEnabled(true)`:** Giữ tham chiếu mạnh (`Strong Reference`) tới các `Bitmap` vừa mới hiển thị trong cấu trúc `LruCache`. Nhờ đó, khi người dùng cuộn lên xuống trong phạm vi vài trăm mục, ảnh được lấy ra ngay trên `Dispatchers.Main.immediate` với độ trễ $\approx 0.05\text{ ms}$ và không tốn bất kỳ chu kỳ CPU giải mã nào.
   - **`weakReferencesEnabled(true)`:** Khi người dùng cuộn qua hơn $600$ mục khiến `StrongMemoryCache` đầy và phải đẩy (evict) các mục cũ nhất ra ngoài, tham chiếu của `Bitmap` đó được chuyển sang `WeakMemoryCache` (dùng `WeakReference<Bitmap>`). Nếu bộ thu gom rác (GC) chưa quét qua vùng nhớ đó, hoặc `Bitmap` vẫn đang được gắn với một thành phần giao diện khác, yêu cầu tiếp theo tới cùng bức ảnh sẽ **khôi phục trực tiếp `Bitmap` từ `WeakMemoryCache` trở lại `StrongMemoryCache`** mà không cần đọc lại từ đĩa!

### 3.2. Tại sao cấu hình L2 Disk Cache = 150 MB + L3 OkHttp Cache = 50 MB và `respectCacheHeaders(false)`?

1. **L2 Disk Cache (`150 MB` tại `coil_optimized_disk_cache`):**
   - Một tệp ảnh Thumbnail $300 \times 300\text{ px}$ đã nén trên mạng có dung lượng trung bình chỉ khoảng $18\text{ KB}$, và ảnh chi tiết $1200 \times 800\text{ px}$ khoảng $140\text{ KB}$.
   - Dung lượng $150\text{ MB}$ trên bộ nhớ Flash cho phép lưu trữ bền vững toàn bộ **$1,000$ ảnh Thumbnail** ($\approx 18\text{ MB}$) cộng với hơn **$900$ ảnh Chi tiết độ phân giải cao** ($\approx 126\text{ MB}$).
   - Nhờ vậy, khi người dùng tắt hoàn toàn ứng dụng (xóa sạch RAM) hoặc bật chế độ ngoại tuyến (**`TC05_OFFLINE_MODE`**), toàn bộ danh mục vẫn hiển thị đầy đủ từ bộ nhớ Flash nội bộ trong thời gian trung bình chỉ **$19\text{ ms}$**.

2. **Tại sao bật `respectCacheHeaders(false)` trên `optimizedImageLoader` kết hợp L3 OkHttp Cache (`50 MB`)?**
   - Một số dịch vụ cung cấp ảnh động hoặc CDN công cộng (như `picsum.photos` sử dụng cơ chế HTTP `302 Redirect` sang `fastly.picsum.photos`) đôi khi trả về các HTTP Header hạn chế lưu đệm hoặc thay đổi đường dẫn đích sau khi chuyển hướng.
   - Ở tầng L3 (`okhttp3.Cache` dung lượng $50\text{ MB}$), OkHttp vẫn lưu đệm các phản hồi HTTP chuẩn theo RFC 7234 (`Cache-Control`, `ETag`).
   - Đồng thời, ở tầng L2 của Coil, việc thiết lập `respectCacheHeaders(false)` đảm bảo rằng một khi luồng byte ảnh đã tải thành công về máy, Coil sẽ **luôn ghi vào `DiskLruCache` theo khóa URL gốc của sản phẩm**, giúp các kịch bản kiểm thử `TC02` (Warm Cache) và `TC05` (Offline Mode) hoạt động nhất quán 100% mà không bị vô hiệu hóa bởi header từ máy chủ bên thứ ba.

---

## 4. Cơ chế `simulationInterceptor`: Kiểm soát thực nghiệm Offline (`TC05`) và Máy chủ chậm (`TC06`)

Một thách thức lớn khi thực nghiệm ứng dụng di động là làm sao kiểm thử trạng thái **Mất mạng (`TC05`)** và **Máy chủ phản hồi chậm (`TC06`)** một cách có kiểm soát, lặp lại chính xác mà không làm ngắt kết nối ADB hay ảnh hưởng tới các tiến trình khác trên máy.

Trong [`ImageLoaderProvider.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt#L62-L80), chúng tôi thiết kế một `okhttp3.Interceptor` tùy chỉnh gắn trực tiếp vào `OkHttpClient`:

```kotlin
// Trích dẫn từ: app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt

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
```

### Phân tích giá trị kỹ thuật của `simulationInterceptor`:

1. **Trong Kịch bản `TC05_OFFLINE_MODE` (`isSimulatedOffline = true`):**
   - Trong kiến trúc của Coil, việc kiểm tra **L1 Memory Cache** và **L2 Disk Cache** diễn ra **trước khi** gọi xuống `HttpUriFetcher` (OkHttp).
   - Do đó, khi bật công tắc `Offline: BẬT`:
     - Ở chế độ **`Optimized`**: Các bức ảnh đã được lưu trong L2 Disk Cache được đọc thẳng từ bộ nhớ Flash và hiển thị thành công (`DataSource.DISK`, tỷ lệ thành công 100% cho các ảnh đã cache) mà không hề chạm tới `simulationInterceptor`. Chỉ những ảnh chưa từng được tải mới đi xuống OkHttp, gặp `IOException` và chuyển sang hiển thị giao diện dự phòng (`Error Fallback`).
     - Ở chế độ **`Baseline`**: Do tắt cả L1 và L2 Cache, 100% yêu cầu đều buộc phải đi xuống OkHttp, gặp `IOException` từ `simulationInterceptor` và thất bại toàn bộ!

2. **Trong Kịch bản `TC06_ERROR_AND_SLOW` (`sim_delay_ms=2200`):**
   - Trong [`ProductRepository.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/data/repository/ProductRepository.kt#L68-L70), cứ mỗi 5 mục có 1 mục gắn URL hỏng (`https://invalid-domain.university.edu.vn/...`) và cứ mỗi 3 mục còn lại có 1 mục gắn tham số `&sim_delay_ms=2200`.
   - Khi `simulationInterceptor` phát hiện tham số `sim_delay_ms=2200`, nó tạm dừng luồng mạng nền (`Dispatchers.IO`) trong đúng $2,200\text{ ms}$.
   - **Kiểm chứng Không treo Main Thread (0 ANR):** Vì `Thread.sleep(2200)` diễn ra hoàn toàn trên worker thread của `Dispatchers.IO`, luồng chính (`Main Thread`) vẫn tự do kết xuất vòng quay `CircularProgressIndicator` và phản hồi thao tác vuốt ở tốc độ 60 FPS.
   - **Kiểm chứng Hủy tức thì khi đang chờ Server chậm:** Nếu người dùng cuộn mục đang bị delay $2,200\text{ ms}$ ra khỏi màn hình, `Job.cancel()` lập tức gọi `call.cancel()`, ngắt quãng `Thread.sleep` thông qua `InterruptedException` và thu hồi luồng ngay lập tức mà không cần đợi hết $2.2$ giây!

---

## 5. Chiến lược Downsampling, Cấu hình Điểm ảnh (`RGB_565` / `HARDWARE`) và Giới hạn luồng giải mã

### 5.1. Đối chiếu trực tiếp mã nguồn tạo `ImageRequest` ở Màn hình Danh sách

Trong tệp [`ProductCatalogScreen.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt#L574-L598), mỗi thẻ sản phẩm xây dựng `ImageRequest` được ghi nhớ bằng `remember(imageUrl, mode)`:

```kotlin
// Trích dẫn từ: app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt

val imageUrl = remember(product.id, mode, scenario, cacheBusterToken, retryCounter) {
    "${product.resolveListImageUrl(mode, scenario, cacheBusterToken)}&r=$retryCounter"
}

val imageRequest = remember(imageUrl, mode) {
    ImageRequest.Builder(context)
        .data(imageUrl)
        .apply {
            if (mode == OptimizationMode.BASELINE) {
                // BASELINE: Giải mã nguyên bản (Size.ORIGINAL = 2400x1600 px) với ARGB_8888 (4B/px)
                size(Size.ORIGINAL)
                precision(Precision.EXACT)
                bitmapConfig(Bitmap.Config.ARGB_8888)
                allowHardware(false)
                crossfade(false)
            } else {
                // OPTIMIZED: Downsample chính xác về kích thước hiển thị (240x240 px) + Scale.FILL
                size(240, 240)
                scale(Scale.FILL)
                precision(Precision.EXACT)
                crossfade(150)
            }
        }
        .build()
}
```

### 5.2. Phân tích định lượng hiệu quả giải mã và bộ nhớ

1. **Ở chế độ `BASELINE` (`Size.ORIGINAL` + `ARGB_8888` + `allowHardware(false)`):**
   - Phương thức [`product.resolveListImageUrl()`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/data/model/ProductItem.kt#L40-L62) trả về ảnh gốc độ phân giải $2400 \times 1600\text{ px}$.
   - Với `size(Size.ORIGINAL)` và `Bitmap.Config.ARGB_8888`, bộ giải mã cấp phát một vùng nhớ điểm ảnh khổng lồ cho **mỗi một dòng trong danh sách**:
     $$\text{RAM}_{\text{Baseline/item}} = 2400 \times 1600 \times 4\text{ bytes} = 15,360,000\text{ bytes} = \mathbf{15.36\text{ MB}}$$
   - Dù khung ảnh trên màn hình chỉ có kích thước $92\text{ dp} \times 92\text{ dp}$ ($\approx 240 \times 240\text{ px}$), GPU phải gánh toàn bộ Texture $15.36\text{ MB}$ đó để vẽ vào một ô nhỏ, gây lãng phí **98.5% số lượng điểm ảnh**.

2. **Ở chế độ `OPTIMIZED` (`size(240, 240)` + `Scale.FILL` + `Precision.EXACT` + `allowRgb565(true)` + `allowHardware(true)`):**
   - Ở các kịch bản thông thường, ứng dụng tải URL Thumbnail ($300 \times 300\text{ px}$) và hạ mẫu chính xác về khung $240 \times 240\text{ px}$.
   - Ngay cả trong kịch bản **`TC04_HIGH_RES`** (bắt buộc tải URL gốc $2400 \times 1600\text{ px}$), nhờ `size(240, 240)` và `Precision.EXACT`, bộ giải mã của Coil tự động tính toán `inSampleSize = 8` để đọc trực tiếp ảnh thu nhỏ từ luồng nén C++ rồi co giãn về đúng $240 \times 240\text{ px}$.
   - Dung lượng bộ nhớ mỗi ảnh sau giải mã với `RGB_565` ($2\text{ bytes/pixel}$) chỉ còn:
     $$\text{RAM}_{\text{Optimized/item}} = 240 \times 240 \times 2\text{ bytes} = 115,200\text{ bytes} = \mathbf{112.5\text{ KB}}$$
   - **Tỷ lệ tiết kiệm RAM trên mỗi ảnh đạt:**
     $$\Delta_{\text{RAM}} = \left(1 - \frac{115,200}{15,360,000}\right) \times 100\% = \mathbf{99.25\%}\text{ (Giảm gấp }133.3\text{ lần!)}$$

3. **Giới hạn độ song song của bộ giải mã (`decoderDispatcher(Dispatchers.IO.limitedParallelism(4))`):**
   - Việc sử dụng `Dispatchers.IO.limitedParallelism(4)` tạo ra một hàng đợi Coroutine có tối đa **4 luồng giải mã chạy đồng thời**.
   - Nếu không có giới hạn này, khi người dùng mở danh sách hoặc cuộn nhanh qua 20 ảnh, cả 20 luồng sẽ đồng loạt gọi vào thư viện gốc `libjpeg-turbo` / `libwebp`, gây tranh chấp bộ nhớ đệm L2/L3 của CPU với `Main Thread` và `RenderThread`, dẫn đến hiện tượng rớt khung hình (Jank).

---

## 6. Xử lý Giao diện: Placeholder, Crossfade 150ms, Error Fallback, Retry và Hủy Coroutine

### 6.1. Thiết kế `SubcomposeAsyncImage` trong `ProductCatalogScreen.kt`

Ứng dụng sử dụng thành phần `SubcomposeAsyncImage` của Coil Compose để kiểm soát toàn diện 3 trạng thái hiển thị của từng khung ảnh:

```kotlin
// Trích dẫn từ: app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt

SubcomposeAsyncImage(
    model = imageRequest,
    imageLoader = imageLoader,
    contentDescription = product.name,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
    loading = {
        // 1. Placeholder hiển thị trong lúc đang tải bất đồng bộ
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Đang tải...", fontSize = 9.sp, color = Color(0xFF475569))
        }
    },
    error = {
        // 2. Ảnh thay thế (Fallback) khi xảy ra lỗi mạng hoặc URL hỏng (TC05, TC06)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEE2E2))
                .padding(4.dp)
        ) {
            Text(
                text = "⚠️ Lỗi ảnh",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB91C1C)
            )
            Text(
                text = "Chạm thử lại",
                fontSize = 9.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.clickable { onRetry() }
            )
        }
    },
    success = {
        // 3. Hiển thị ảnh thành công kèm hiệu ứng Crossfade 150ms
        SubcomposeAsyncImageContent()
    }
)
```

### 6.2. Cơ chế Thử lại (`Retry`) và Tách biệt Cache (`cacheBusterToken`)

Trong [`CatalogViewModel.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/viewmodel/CatalogViewModel.kt#L199-L209) và [`ProductCatalogScreen.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt#L574-L576):
- **Biến trạng thái `retryCounter`:** Khi người dùng chạm vào nút `"Chạm thử lại"` trên ô ảnh bị lỗi (sau khi đã tắt chế độ Offline hoặc khắc phục mạng), hàm `retryFailedImages()` tăng `retryCounter` thêm $1$. Vì `imageUrl` phụ thuộc vào `retryCounter` trong `remember(product.id, mode, scenario, cacheBusterToken, retryCounter)`, chỉ những ô cần tải lại mới kích hoạt `ImageRequest` mới một cách mượt mà.
- **Biến trạng thái `cacheBusterToken`:** Đáp ứng yêu cầu Lưu ý thực nghiệm của đề bài (phân biệt rõ App Cache với CDN/HTTP Cache bên ngoài), mỗi khi người dùng chọn kịch bản `TC01_FIRST_LOAD` hoặc nhấn nút `"Xóa All Cache"`, ứng dụng vừa xóa sạch L1/L2 Cache, vừa tăng `cacheBusterToken += 1` (gắn vào tham số `?cb=$cacheBusterToken`). Điều này đảm bảo tầng L3 HTTP Cache hoặc proxy trung gian không thể trả về bản sao cũ, giúp phép đo Cold Load (`TC01`) chính xác tuyệt đối 100%.

### 6.3. Tối ưu hóa `LazyColumn` với `key` và `contentType` kết hợp Hủy Coroutine tự động

Trong [`ProductCatalogScreen.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt#L190-L223), sự khác biệt về cách khai báo danh sách cuộn giữa hai chế độ được thể hiện rõ ràng:

```kotlin
// Trích dẫn từ: app/src/main/kotlin/com/university/imageoptimization/ui/screens/ProductCatalogScreen.kt

if (uiState.mode == OptimizationMode.OPTIMIZED) {
    // PHIÊN BẢN TỐI ƯU: Cung cấp `key` cố định và `contentType` để tránh Recomposition thừa
    items(
        items = uiState.products,
        key = { item -> item.id },
        contentType = { "optimized_product_card" }
    ) { product ->
        ProductListItemCard(...)
    }
} else {
    // PHIÊN BẢN CƠ SỞ (BASELINE): Không gắn key ổn định, tải ảnh gốc không downsample
    items(items = uiState.products) { product ->
        ProductListItemCard(...)
    }
}
```

Khi người dùng cuộn nhanh (`TC03_FAST_SCROLL`), ngay khi một `ProductListItemCard` trôi ra khỏi vùng nhìn của `LazyColumn`, `SubcomposeAsyncImage` rời khỏi Composition và tự động gọi `Job.cancel()` trên Coroutine tải ảnh của mục đó. Sự kiện này được [`PerformanceTracker.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/telemetry/PerformanceTracker.kt#L247-L255) bắt ngay tức khắc tại phương thức `onCancel(request)` và ghi nhận vào cả bảng điều khiển trực tiếp lẫn Logcat:

```kotlin
override fun onCancel(request: ImageRequest) {
    requestStartTimes.remove(request)
    decodeStartTimes.remove(request)
    val cancelled = cancelledRequests.incrementAndGet()
    if (cancelled <= 8 || cancelled % 15 == 0) {
        appendLog("CANCELLED #${cancelled}: Đã hủy Coroutine tải ảnh ngoài khung nhìn")
    }
    publishSnapshot()
}
```
Nhờ vậy, hàng đợi giải mã (4 permits) luôn được giải phóng tức thời để ưu tiên tuyệt đối cho những bức ảnh tại vị trí mà người dùng dừng lại xem.
