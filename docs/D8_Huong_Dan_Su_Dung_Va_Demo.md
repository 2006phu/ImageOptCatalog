# HƯỚNG DẪN SỬ DỤNG, TÁI HIỆN THỰC NGHIỆM VÀ KỊCH BẢN DEMO (DELIVERABLE D8)

**Đề tài 02:** Tối ưu tải, lưu trữ đệm và hiển thị hình ảnh trong ứng dụng Android  
**Tên sản phẩm:** `ImageOptCatalog` (`com.university.imageoptimization`)  
**Tệp cài đặt đóng gói sẵn:** `releases/ImageOptCatalog-debug.apk`  

---

## MỤC LỤC

1. [Bảng Ánh xạ Toàn bộ 8 Sản phẩm Đầu ra (Deliverables D1 – D8)](#1-bảng-ánh-xạ-toàn-bộ-8-sản-phẩm-đầu-ra-deliverables-d1--d8)
2. [Hướng dẫn Cài đặt Nhanh APK & Biên dịch từ Mã nguồn](#2-hướng-dẫn-cài-đặt-nhanh-apk--biên-dịch-từ-mã-nguồn)
3. [Hướng dẫn Chuyển đổi giữa 2 Phiên bản `Baseline` và `Optimized`](#3-hướng-dẫn-chuyển-đổi-giữa-2-phiên-bản-baseline-và-optimized)
4. [Hướng dẫn Từng bước Tái hiện 8 Kịch bản Thực nghiệm (`TC01` – `TC08`)](#4-hướng-dẫn-từng-bước-tái-hiện-8-kịch-bản-thực-nghiệm-tc01--tc08)
5. [Hướng dẫn Quan sát bằng Android Studio Profiler & Logcat](#5-hướng-dẫn-quan-sát-bằng-android-studio-profiler--logcat)
6. [Kịch bản Quay Video Demo Báo cáo (3 – 5 Phút)](#6-kịch-bản-quay-video-demo-báo-cáo-3--5-phút)

---

## 1. Bảng Ánh xạ Toàn bộ 8 Sản phẩm Đầu ra (Deliverables D1 – D8)

Dự án đã hoàn thiện đầy đủ 100% các tài liệu học thuật, mã nguồn, bộ kiểm thử và gói cài đặt theo đúng yêu cầu của Đề tài 02:

| Mã Deliverable | Tên sản phẩm đầu ra theo yêu cầu đề bài | Đường dẫn tệp trong Repository | Nội dung trọng tâm |
| :---: | :--- | :--- | :--- |
| **`D1`** | **Báo cáo nghiên cứu lý thuyết** | [`docs/D1_Bao_Cao_Nghien_Cuu_Ly_Thuyet.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D1_Bao_Cao_Nghien_Cuu_Ly_Thuyet.md) | Phân tích học thuật chuyên sâu 10 chủ đề: Pipeline 5 bước, Coroutines, Cache 3 tầng (L1/L2/L3), JPEG/PNG/WebP, công thức tính RAM `Bitmap`, Lazy Loading, `LazyColumn` vs `RecyclerView`, Downsampling, Profiling và Phương pháp thực nghiệm. |
| **`D2`** | **Tài liệu phân tích và thiết kế** | [`docs/D2_Tai_Lieu_Phan_Tich_Thiet_Ke.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D2_Tai_Lieu_Phan_Tich_Thiet_Ke.md) | Đặc tả yêu cầu chức năng/phi chức năng, kiến trúc MVVM + Telemetry Engine, Sequence Diagram luồng tải/hủy bất đồng bộ, Flowchart bộ nhớ đệm 3 tầng và thiết kế 8 kịch bản kiểm thử. |
| **`D3`** | **Ứng dụng Android minh họa (`ImageOptCatalog`)** | [`app/src/main/kotlin/com/university/imageoptimization/`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/) | Ứng dụng Jetpack Compose hoàn chỉnh gồm: Danh sách sản phẩm (`100 / 500 / 1.000` ảnh), Màn hình chi tiết chẩn đoán `Bitmap`, Bảng điều khiển Telemetry thời gian thực và Hộp thoại báo cáo. |
| **`D4`** | **Mô-đun tải ảnh và bộ nhớ đệm** | [`docs/D4_Mo_Dun_Tai_Anh_Va_Cache.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D4_Mo_Dun_Tai_Anh_Va_Cache.md)<br>[`ImageLoaderProvider.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/ImageLoaderProvider.kt) | Cài đặt và giải thích chi tiết cấu hình L1 `LruCache` (25% RAM) + `WeakMemoryCache`, L2 `DiskLruCache` (150 MB), L3 `okhttp3.Cache` (50 MB), `simulationInterceptor`, Downsampling `240x240` và `RGB_565` / `HARDWARE`. |
| **`D5`** | **Hai phiên bản đối chứng Trước & Sau tối ưu (`baseline` vs `optimized`)** | [`OptimizationMode.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/OptimizationMode.kt)<br>Nhánh Git: `baseline` & `optimized` | Hỗ trợ đối chứng theo 2 cách: (1) Chuyển đổi trực tiếp bằng nút bấm trên giao diện ứng dụng, hoặc (2) Chuyển nhánh Git `git checkout baseline` / `git checkout optimized`. |
| **`D6`** | **Bộ kịch bản và kết quả kiểm thử** | [`docs/D6_Kich_Ban_Va_Ket_Qua_Kiem_Thu.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D6_Kich_Ban_Va_Ket_Qua_Kiem_Thu.md)<br>[`ImageOptimizationUnitTest.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/test/kotlin/com/university/imageoptimization/ImageOptimizationUnitTest.kt) | Đặc tả và kết quả kiểm thử PASS toàn bộ 8 kịch bản `TC01` – `TC08`, nhật ký thực tế `[BENCHMARK_LOG]` và 4 bài kiểm thử đơn vị JUnit. |
| **`D7`** | **Báo cáo thực nghiệm và phân tích hiệu năng** | [`docs/D7_Bao_Cao_Thuc_Nghiem_Hieu_Nang.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D7_Bao_Cao_Thuc_Nghiem_Hieu_Nang.md)<br>[`PerformanceTracker.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/telemetry/PerformanceTracker.kt) | Số liệu thực nghiệm đo lặp lại 5 lần ($\bar{x} \pm s$), biểu đồ Mermaid `xychart-beta`, phân tích mở rộng quy mô $100 \rightarrow 500 \rightarrow 1.000$ ảnh và phân tích các đánh đổi kỹ thuật (Trade-offs). |
| **`D8`** | **Sản phẩm đóng gói & Hướng dẫn sử dụng / Demo** | [`docs/D8_Huong_Dan_Su_Dung_Va_Demo.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/docs/D8_Huong_Dan_Su_Dung_Va_Demo.md)<br>[`README.md`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/README.md)<br>`releases/ImageOptCatalog-debug.apk` | Tài liệu hướng dẫn cài đặt, biên dịch, vận hành thực nghiệm và kịch bản quay Video Demo 3–5 phút. |

---

## 2. Hướng dẫn Cài đặt Nhanh APK & Biên dịch từ Mã nguồn

### Cách 1: Cài đặt trực tiếp từ tệp APK đóng gói sẵn (Nhanh nhất)
Tệp APK đã được biên dịch sẵn nằm tại đường dẫn `releases/ImageOptCatalog-debug.apk`. Để cài đặt lên máy ảo (Android Emulator) hoặc điện thoại thật đang bật USB Debugging:

```powershell
adb install -r releases/ImageOptCatalog-debug.apk
```

Hoặc kéo-thả trực tiếp tệp `releases/ImageOptCatalog-debug.apk` vào cửa sổ **Android Emulator**.

### Cách 2: Biên dịch và chạy từ mã nguồn bằng Gradle / Android Studio
1. **Yêu cầu hệ thống:**
   - JDK 17 (Android Studio Ladybug / Koala trở lên đi kèm sẵn JBR 17/21).
   - Android SDK Platform 34 (Android 14) & Build-Tools 34.0.0.
2. **Các lệnh biên dịch và kiểm thử qua Terminal (PowerShell / CMD):**
   ```powershell
   # 1. Chạy bộ kiểm thử đơn vị (Unit Tests - Deliverable D6)
   .\gradlew.bat testDebugUnitTest

   # 2. Biên dịch gói cài đặt APK Debug
   .\gradlew.bat assembleDebug

   # 3. Cài đặt trực tiếp lên thiết bị đang kết nối
   .\gradlew.bat installDebug
   ```

---

## 3. Hướng dẫn Chuyển đổi giữa 2 Phiên bản `Baseline` và `Optimized`

Đáp ứng Yêu cầu 5 (Deliverable D5), dự án hỗ trợ **2 phương thức** chuyển đổi giữa bản Chưa tối ưu (`Baseline`) và bản Đã tối ưu (`Optimized`):

### Phương thức 1: Chuyển đổi trực tiếp theo thời gian thực ngay trên giao diện (Khuyến nghị khi Demo)
Ngay trên bảng điều khiển `BenchmarkControlDashboard` ở nửa trên màn hình ứng dụng, có 2 nút chọn lớn:
- **Nút màu cam `Chưa tối ưu (Baseline)`:** Chuyển ngay lập tức sang `baselineImageLoader` (Tải ảnh gốc $2400 \times 1600$, giải mã `Size.ORIGINAL` + `ARGB_8888`, tắt L1/L2 Cache, tắt `LazyColumn` key).
- **Nút màu xanh lá `Đã tối ưu (Optimized)`:** Chuyển ngay lập tức sang `optimizedImageLoader` (Tải Thumbnail $300 \times 300$, Downsample $240 \times 240$, bật `RGB_565` / `HARDWARE`, bật L1 25% RAM + L2 150MB Disk + L3 50MB HTTP, bật `LazyColumn` `key` & `contentType`).

### Phương thức 2: Chuyển đổi qua 2 nhánh Git (`baseline` và `optimized`)
Trong kho lưu trữ Git của dự án đã được thiết lập sẵn 2 nhánh độc lập quy định chế độ khởi động mặc định thông qua trường `BuildConfig.DEFAULT_MODE` trong `app/build.gradle.kts`:

```powershell
# Chuyển sang nhánh phiên bản Cơ sở chưa tối ưu (Khởi động mặc định ở chế độ BASELINE)
git checkout baseline

# Chuyển sang nhánh phiên bản Đã tối ưu (Khởi động mặc định ở chế độ OPTIMIZED)
git checkout optimized
```

---

## 4. Hướng dẫn Từng bước Tái hiện 8 Kịch bản Thực nghiệm (`TC01` – `TC08`)

Giao diện chính của ứng dụng được thiết kế như một **Bàn điều khiển Phòng thí nghiệm (Benchmark Lab)** gồm 4 ô chỉ số thời gian thực ở hàng đầu tiên (`BỘ NHỚ RAM`, `THỜI GIAN TẢI`, `CACHE HIT (L1/L2)`, `ĐỘ MƯỢT UI`) và dãy 8 chip kịch bản `TC01` $\rightarrow$ `TC08`.

### Quy trình tái hiện chuẩn từng kịch bản:

1. **Kịch bản `TC01` (Cold Load - Mở danh sách lần đầu):**
   - Chọn chế độ `Chưa tối ưu (Baseline)` $\rightarrow$ Nhấn chip **`TC01`** (Ứng dụng tự động xóa sạch RAM + Disk Cache và đổi token URL mới).
   - Cuộn danh sách xuống khoảng 20–30 mục $\rightarrow$ Quan sát ô `THỜI GIAN TẢI` ($\sim 600 - 700\text{ ms}$) và `BỘ NHỚ RAM` ($\sim 240+\text{ MB}$).
   - Chuyển sang `Đã tối ưu (Optimized)` $\rightarrow$ Nhấn chip **`TC01`** $\rightarrow$ Cuộn tương tự $\rightarrow$ Quan sát thời gian tải giảm xuống chỉ còn $\sim 180\text{ ms}$ và RAM $\sim 76\text{ MB}$.

2. **Kịch bản `TC02` (Warm/Hot Cache - Mở lại khi Cache còn hiệu lực):**
   - Đang ở `Optimized` (sau khi đã tải ảnh ở `TC01`), nhấn chip **`TC02`** rồi cuộn lên xuống $\rightarrow$ Quan sát `CACHE HIT` vọt lên **$95\% - 100\%$** (`R:` tăng mạnh), thời gian tải trung bình chỉ **$1 - 8\text{ ms}$**.
   - Nhấn nút **`Xóa RAM`** (chỉ xóa L1 RAM, giữ nguyên L2 Disk Cache) rồi cuộn lại $\rightarrow$ Quan sát bộ đếm `D:` (Disk Cache Hit) tăng lên và ảnh hiển thị trong $\sim 18\text{ ms}$ mà không tốn một byte mạng nào (`N:0`).

3. **Kịch bản `TC03` (Fast Fling Scroll - Cuộn nhanh qua nhiều ảnh):**
   - Chọn chip **`TC03`** $\rightarrow$ Nhấn nút **`▶ Auto-Scroll (TC03)`** màu xanh ngọc.
   - Hệ thống tự động cuộn tốc độ cao xuyên suốt danh sách rồi cuộn ngược về đầu.
   - Quan sát dòng chữ chạy trực tiếp: `Coroutines — Xong: ... | Đã hủy (Cancelled): ...` và đối chiếu `Jank %` giữa `Baseline` ($\sim 34\%$) và `Optimized` ($\sim 4\%$).

4. **Kịch bản `TC04` (Ảnh độ phân giải lớn $2400 \times 1600\text{ px}$):**
   - Chọn chip **`TC04`** (ép cả 2 chế độ phải tải URL ảnh gốc độ phân giải cao `/2400/1600`).
   - So sánh nhãn hiển thị ngay trên từng thẻ sản phẩm: `Full 2400px (~15MB)` ở bản `Baseline` so với `Thumb 240px (~132KB)` ở bản `Optimized` nhờ kỹ thuật Downsampling (`inSampleSize`).

5. **Kịch bản `TC05` (Offline Mode - Tắt mạng khi mở danh sách):**
   - Đảm bảo đã xem qua một số sản phẩm ở chế độ `Optimized`, sau đó nhấn chip **`TC05`** (Công tắc `Offline: BẬT` tự động kích hoạt và RAM Cache bị xóa để kiểm tra Disk Cache).
   - Cuộn qua các mục đã xem: Bản `Optimized` hiển thị ảnh bình thường từ L2 Disk Cache; các mục chưa từng tải sẽ hiện khung màu đỏ nhạt `⚠️ Lỗi ảnh - Chạm thử lại`.
   - Gạt công tắc `Offline: TẮT` rồi chạm vào `"Chạm thử lại"` để thấy ảnh khôi phục tức thì.

6. **Kịch bản `TC06` (URL ảnh lỗi 20% & Máy chủ chậm 30% delay 2.2s):**
   - Nhấn chip **`TC06`** $\rightarrow$ Cuộn danh sách:
     - Các sản phẩm thứ `#3, #6, #9...` bị giữ chậm $2,200\text{ ms}$ trên luồng nền (hiển thị vòng xoay `Đang tải...`) nhưng việc cuộn màn hình vẫn đạt $60\text{ FPS}$ mượt mà, chứng minh không gây treo Main Thread (0 ANR).
     - Các sản phẩm thứ `#5, #10, #15...` gắn tên miền hỏng tự động chuyển sang giao diện `⚠️ Lỗi ảnh`.

7. **Kịch bản `TC07` (Khả năng mở rộng: $100 \rightarrow 500 \rightarrow 1.000$ ảnh):**
   - Bấm vào các nút quy mô **`100 ảnh`** $\rightarrow$ **`500 ảnh`** $\rightarrow$ **`1000 ảnh`** (hoặc bấm chip **`TC07`**).
   - Nhấn **`▶ Auto-Scroll (TC03)`** trên danh sách $1,000$ ảnh để kiểm chứng RAM của bản `Optimized` giữ ổn định quanh mức $\sim 108\text{ MB}$ nhờ cơ chế thu hồi tự động của `LruCache`.

8. **Kịch bản `TC08` (Bảng báo cáo tổng hợp & Chẩn đoán Chi tiết sản phẩm):**
   - Chạm vào bất kỳ sản phẩm nào để mở **Màn hình Chi tiết (`ProductDetailScreen`)**: Xem bảng `🔬 Phân Tích Giải Mã Ảnh & Bộ Nhớ` hiển thị chính xác kích thước `Bitmap` sau giải mã, cấu hình `Bitmap.Config` (`HARDWARE` / `RGB_565` vs `ARGB_8888`) và số byte trong RAM.
   - Quay lại màn hình chính và nhấn nút **`📊 Bảng TC01-08`** (hoặc chip **`TC08`**) để mở bảng đối chiếu tổng kết toàn bộ 8 kịch bản.

---

## 5. Hướng dẫn Quan sát bằng Android Studio Profiler & Logcat

1. **Quan sát Nhật ký Đo lường qua Logcat:**
   - Mở tab **Logcat** trong Android Studio và nhập bộ lọc:
     ```text
     tag:ImageOptBenchmark
     ```
   - Mọi sự kiện tải thành công (`OK #n: MEMORY_CACHE | DISK | NETWORK`), thời gian giải mã (`ms`), kích thước `Bitmap` (`240x240 HARDWARE 112KB` vs `2400x1600 ARGB_8888 15000KB`), sự kiện hủy Coroutine (`CANCELLED #n`) và sự kiện lỗi (`ERROR #n`) đều được in ra theo thời gian thực.

2. **Quan sát Bộ nhớ qua Android Studio Memory Profiler:**
   - Mở **View $\rightarrow$ Tool Windows $\rightarrow$ Profiler** $\rightarrow$ Chọn tiến trình `com.university.imageoptimization` $\rightarrow$ Chọn **Memory**.
   - Khi chạy `Baseline` + `Auto-Scroll`, quan sát vùng **Native** và **Graphics** tăng dựng đứng lên $> 300\text{ MB}$ kèm các biểu tượng thùng rác GC liên tục.
   - Khi chuyển sang `Optimized` + `Xóa All Cache` + `Auto-Scroll`, quan sát đường bộ nhớ thấp và phẳng ($\sim 80 - 108\text{ MB}$).

---

## 6. Kịch bản Quay Video Demo Báo cáo (3 – 5 Phút)

Để bài thuyết trình và video demo đạt hiệu quả thị giác và điểm số cao nhất, nhóm thực hiện theo kịch bản 5 phân cảnh chuẩn dưới đây:

| Phân cảnh | Thời lượng | Thao tác thực hiện trên màn hình | Nội dung thuyết minh trọng tâm |
| :--- | :---: | :--- | :--- |
| **Cảnh 1: Giới thiệu tổng quan & Kiến trúc** | `00:00 – 00:40` | Mở ứng dụng `ImageOptCatalog`, giới thiệu thanh trạng thái 4 ô Telemetry (`RAM`, `Thời gian tải`, `Cache Hit`, `FPS/Jank`), 2 nút chuyển đổi `Baseline` / `Optimized`, bộ chọn quy mô `100 / 500 / 1000 ảnh` và 8 chip `TC01`–`TC08`. | Giới thiệu mục tiêu Đề tài 02 và cách ứng dụng tích hợp trực tiếp `EventListener` của Coil cùng `Choreographer.FrameCallback` để đo lường khách quan ngay trên giao diện. |
| **Cảnh 2: Đối chứng Tải lần đầu (`TC01`) & Ảnh lớn (`TC04`)** | `00:40 – 01:45` | 1. Chọn `Baseline` $\rightarrow$ Bấm `TC01` $\rightarrow$ Cuộn danh sách (chỉ vào nhãn `Full 2400px (~15MB)`, thời gian tải $\sim 640\text{ ms}$, RAM vọt lên $> 240\text{ MB}$).<br>2. Chuyển sang `Optimized` $\rightarrow$ Bấm `TC01` $\rightarrow$ Cuộn danh sách (chỉ vào nhãn `Thumb 240px (~112KB)`, thời gian tải giảm còn $\sim 185\text{ ms}$, RAM chỉ $\sim 76\text{ MB}$). | Giải thích công thức $\text{Width} \times \text{Height} \times \text{BytesPerPixel}$: Tại sao 1 ảnh $2400 \times 1600$ `ARGB_8888` tốn $15.36\text{ MB}$ RAM, trong khi tách Thumbnail + Downsampling `240x240` `RGB_565`/`HARDWARE` chỉ tốn $112.5\text{ KB}$ (tiết kiệm $>99\%$ RAM/ảnh). |
| **Cảnh 3: Kiểm chứng Bộ nhớ đệm 3 tầng (`TC02`) & Offline Mode (`TC05`)** | `01:45 – 02:45` | 1. Bấm `TC02` ở chế độ `Optimized`, cuộn lên xuống để thấy `CACHE HIT: 98%` và thời gian tải chỉ `1–8 ms`.<br>2. Bấm `TC05` (tự động bật `Offline: BẬT` và xóa RAM Cache), cuộn qua các mục đã xem để chứng minh ảnh vẫn lên tức thì từ **L2 Disk Cache (`19 ms`)**; cuộn xuống mục mới để thấy `⚠️ Lỗi ảnh`, sau đó tắt Offline và bấm `"Chạm thử lại"`. | Làm rõ vai trò của từng tầng Cache: L1 `LruCache` (RAM) phục vụ cuộn tức thì, L2 `DiskLruCache` ($150\text{ MB}$) bảo toàn dữ liệu khi tắt app hoặc mất mạng. |
| **Cảnh 4: Cuộn nhanh Fling (`TC03`), Server chậm (`TC06`) & Mở rộng 1.000 ảnh (`TC07`)** | `02:45 – 03:50` | 1. Chọn quy mô **`1000 ảnh`** (`TC07`) $\rightarrow$ Bấm **`▶ Auto-Scroll (TC03)`**.<br>2. Chỉ vào bộ đếm `Đã hủy (Cancelled)` tăng liên tục trên Dashboard và Logcat, đồng thời tốc độ khung hình vẫn giữ `58–60 FPS` (Jank $< 5\%$).<br>3. Bấm nhanh `TC06` để cho thấy các mục delay $2.2\text{s}$ không làm đơ giao diện. | Giải thích cơ chế Structured Concurrency: Khi mục cuộn ra khỏi `LazyColumn`, Coroutine tự động bị `Job.cancel()`, nhường CPU và mạng cho các mục tại điểm dừng. |
| **Cảnh 5: Chẩn đoán `Bitmap` ở Màn hình Chi tiết & Bảng Tổng kết (`TC08`)** | `03:50 – 04:45` | 1. Chạm vào 1 sản phẩm để mở `ProductDetailScreen`, chỉ vào bảng `🔬 Phân Tích Giải Mã Ảnh & Bộ Nhớ`.<br>2. Quay lại màn hình chính, bấm nút **`📊 Bảng TC01-08`** để mở hộp thoại tổng hợp số liệu Trước & Sau tối ưu và kết luận. | Tổng kết mức cải thiện toàn diện: Giảm **79.5%** thời gian tải, tiết kiệm **69.5%** RAM đỉnh, giảm **87.9%** hiện tượng giật khung hình và nêu các đánh đổi kỹ thuật (Trade-offs). |
