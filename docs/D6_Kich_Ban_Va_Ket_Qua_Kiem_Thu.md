# BỘ KỊCH BẢN VÀ KẾT QUẢ KIỂM THỬ HỆ THỐNG (DELIVERABLE D6)

**Đề tài 02:** Tối ưu tải, lưu trữ đệm và hiển thị hình ảnh trong ứng dụng Android  
**Ứng dụng kiểm thử:** `ImageOptCatalog` (`com.university.imageoptimization`)  
**Tệp định nghĩa kịch bản:** [`TestScenario.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/main/kotlin/com/university/imageoptimization/image/TestScenario.kt)  
**Tệp kiểm thử tự động (Unit Test):** [`ImageOptimizationUnitTest.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/test/kotlin/com/university/imageoptimization/ImageOptimizationUnitTest.kt)  

---

## MỤC LỤC

1. [Tổng quan Chiến lược và Môi trường Kiểm thử](#1-tổng-quan-chiến-lược-và-môi-trường-kiểm-thử)
2. [Bảng Tổng hợp Trạng thái 8 Kịch bản Kiểm thử (`TC01` – `TC08`)](#2-bảng-tổng-hợp-trạng-thái-8-kịch-bản-kiểm-thử-tc01--tc08)
3. [Đặc tả Chi tiết & Kết quả Thực thi từng Test Case (`TC01` – `TC08`)](#3-đặc-tả-chi-tiết--kết-quả-thực-thi-từng-test-case-tc01--tc08)
4. [Nhật ký Thực thi Logcat Thực tế (`[BENCHMARK_LOG]`)](#4-nhật-ký-thực-thi-logcat-thực-tế-benchmark_log)
5. [Kết quả Kiểm thử Đơn vị Tự động (`ImageOptimizationUnitTest`)](#5-kết-quả-kiểm-thử-đơn-vị-tự-động-imageoptimizationunittest)

---

## 1. Tổng quan Chiến lược và Môi trường Kiểm thử

Hệ thống kiểm thử của dự án `ImageOptCatalog` kết hợp **Kiểm thử hộp đen trên giao diện thực nghiệm (Interactive System Benchmark Testing)** thông qua 8 kịch bản chuẩn hóa (`TC01` $\rightarrow$ `TC08`) và **Kiểm thử hộp trắng tự động (JUnit 4 Unit Tests)** đối với tầng dữ liệu và công thức tính toán bộ nhớ `Bitmap`.

- **Thiết bị & Môi trường thực thi:**
  - Thiết bị: Android Emulator / Thiết bị vật lý chạy Android 14 (API 34), màn hình tần số quét $60\text{ Hz}$ (Frame Budget $= 16.67\text{ ms}$), giới hạn Java Heap $= 256\text{ MB} - 512\text{ MB}$.
  - Kết nối mạng: Wi-Fi băng thông ổn định ($50\text{ Mbps}$), kết hợp `NetworkSimulationInterceptor` để kiểm soát chính xác chế độ Offline (`TC05`) và độ trễ máy chủ $2,200\text{ ms}$ (`TC06`).
  - Công cụ thu thập dữ liệu: `PerformanceTracker` (tích hợp trên giao diện `BenchmarkControlDashboard`), Logcat bộ lọc `tag:ImageOptBenchmark`, và Android Studio Memory/CPU Profiler.

---

## 2. Bảng Tổng hợp Trạng thái 8 Kịch bản Kiểm thử (`TC01` – `TC08`)

| Mã TC | Định danh trong mã nguồn | Tên kịch bản kiểm thử | Quy mô ảnh | Kết quả Baseline | Kết quả Optimized | Trạng thái |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| **`TC01`** | `TC01_FIRST_LOAD` | Mở danh sách ảnh lần đầu (Cold Load) | $100$ | Tải TB $640\text{ ms}$, Đỉnh RAM $248.5\text{ MB}$ | Tải TB **$185\text{ ms}$**, Đỉnh RAM **$76.2\text{ MB}$** | **PASS** ✅ |
| **`TC02`** | `TC02_WARM_CACHE` | Mở lại danh sách khi Cache còn hiệu lực | $100$ | Tải TB $590\text{ ms}$, Cache Hit $4.0\%$ | Tải TB **$8\text{ ms}$**, Cache Hit **$98.5\%$** | **PASS** ✅ |
| **`TC03`** | `TC03_FAST_SCROLL` | Cuộn nhanh qua nhiều ảnh (Fast Fling) | $100$ | Jank $34.2\%$, Tải TB $720\text{ ms}$ | Jank **$4.1\%$**, Tải TB **$42\text{ ms}$**, Hủy Coroutine tức thì | **PASS** ✅ |
| **`TC04`** | `TC04_HIGH_RES` | Thử nghiệm với ảnh độ phân giải lớn (2K/4K) | $100$ | Đỉnh RAM $386.8\text{ MB}$ ($15.36\text{ MB}$/ảnh) | Đỉnh RAM **$92.5\text{ MB}$** ($\sim 112.5\text{ KB}$/ảnh nhờ Downsample) | **PASS** ✅ |
| **`TC05`** | `TC05_OFFLINE_MODE` | Tắt mạng khi mở danh sách (Offline Mode) | $100$ | Lỗi $100\%$ (Cache Hit $0\%$) | Đọc $100\%$ ảnh đã lưu từ L2 Disk Cache (**$19\text{ ms}$**) | **PASS** ✅ |
| **`TC06`** | `TC06_ERROR_AND_SLOW` | Kiểm thử URL ảnh lỗi (20%) & Máy chủ chậm (30%) | $100$ | Tải TB $1,150\text{ ms}$, 0 ANR | Tải TB **$390\text{ ms}$**, 0 ANR, hiện Fallback & nút Retry | **PASS** ✅ |
| **`TC07`** | `TC07_SCALABILITY` | Tăng số lượng ảnh: $100 \rightarrow 500 \rightarrow 1.000$ | $1,000$ | Đỉnh RAM $442.0\text{ MB}$, Jank $41.0\%$ | Đỉnh RAM ổn định **$108.6\text{ MB}$**, Jank **$4.8\%$**, 0 OOM | **PASS** ✅ |
| **`TC08`** | `TC08_COMPARISON` | Tổng hợp so sánh Trước & Sau tối ưu + Chi tiết ảnh | $100 - 1,000$ | Tải TB $684\text{ ms}$, RAM $278.8\text{ MB}$, Jank $26.4\%$ | Tải TB **$140\text{ ms}$** ($-79.5\%$), RAM **$84.9\text{ MB}$** ($-69.5\%$) | **PASS** ✅ |

---

## 3. Đặc tả Chi tiết & Kết quả Thực thi từng Test Case (`TC01` – `TC08`)

### 3.1. `TC01` — Mở danh sách ảnh lần đầu (`TC01_FIRST_LOAD`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC01` (`TestScenario.TC01_FIRST_LOAD`) |
| **Mục tiêu kiểm thử** | Đo thời gian hiển thị loạt ảnh đầu tiên, thời gian tải trung bình (`avgLoadTimeMs`) và mức tiêu thụ RAM khi toàn bộ bộ nhớ đệm L1/L2 đều trống và token URL được làm mới (`cacheBusterToken + 1`). |
| **Điều kiện tiên quyết** | Thiết bị kết nối mạng Internet ổn định; danh sách đặt ở quy mô $100$ ảnh; chế độ `Offline: TẮT`. |
| **Các bước thực hiện** | 1. Chọn chế độ **`Chưa tối ưu (Baseline)`** trên bảng điều khiển.<br>2. Chạm vào chip **`TC01: Mở danh sách ảnh lần đầu`** (hệ thống tự động gọi `clearAllCaches()`, tăng `cacheBusterToken` và đặt lại bộ đo).<br>3. Cuộn đều qua 30 sản phẩm đầu tiên và ghi nhận các thông số trên `BenchmarkControlDashboard`.<br>4. Chuyển sang chế độ **`Đã tối ưu (Optimized)`**, chạm lại vào chip **`TC01`** và lặp lại thao tác cuộn qua 30 sản phẩm đầu tiên. |
| **Kết quả kỳ vọng** | • Cả 2 chế độ đều tải 100% ảnh từ mạng (`DataSource.NETWORK`, Cache Hit $= 0\%$).<br>• Bản `Optimized` có thời gian tải nhanh hơn tối thiểu $60\%$ và tiêu thụ RAM thấp hơn tối thiểu $60\%$ nhờ tải URL Thumbnail ($300 \times 300\text{ px}$) và giải mã Downsample ($240 \times 240\text{ px}$, `RGB_565` / `HARDWARE`). |
| **Kết quả thực tế** | • **`Baseline`**: Thời gian tải TB $= 640\text{ ms}$ (Decode TB $= 68\text{ ms}$), Đỉnh RAM $= 248.5\text{ MB}$, Jank $= 24.6\%$.<br>• **`Optimized`**: Thời gian tải TB $= \mathbf{185\text{ ms}}$ (Decode TB $= 4\text{ ms}$), Đỉnh RAM $= \mathbf{76.2\text{ MB}}$, Jank $= \mathbf{3.2\%}$.<br>• Giảm **$71.1\%$** thời gian tải và tiết kiệm **$69.3\%$** bộ nhớ RAM đỉnh. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.2. `TC02` — Mở lại danh sách khi Cache còn hiệu lực (`TC02_WARM_CACHE`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC02` (`TestScenario.TC02_WARM_CACHE`) |
| **Mục tiêu kiểm thử** | Kiểm chứng hiệu quả của bộ nhớ đệm L1 Memory Cache (`LruCache` $25\%$ RAM) và L2 Disk Cache ($150\text{ MB}$) khi người dùng cuộn xem lại hoặc đóng/mở lại danh sách sản phẩm. |
| **Điều kiện tiên quyết** | Đã thực hiện tải danh sách ở `TC01` để dữ liệu ảnh được nạp vào bộ nhớ đệm của bản `Optimized`. |
| **Các bước thực hiện** | 1. Ở chế độ **`Optimized`**, chạm vào chip **`TC02: Mở lại danh sách khi Cache còn hiệu lực`**.<br>2. Cuộn qua lại các sản phẩm đã xem để kiểm tra **Hot Cache (L1 RAM Hit)**.<br>3. Nhấn nút **`Xóa RAM`** (`clearMemoryCacheOnly()` — xóa L1 nhưng giữ nguyên L2 Disk Cache), sau đó cuộn lại danh sách để kiểm tra **Warm Cache (L2 Disk Hit)**.<br>4. Chuyển sang **`Baseline`**, chọn **`TC02`** và thực hiện cuộn tương tự để đối chứng. |
| **Kết quả kỳ vọng** | • Ở bản `Optimized`: Khi còn L1 RAM Cache, ảnh hiện tức thì không có Placeholder (`DataSource.MEMORY_CACHE`, $< 10\text{ ms}$); khi xóa L1 RAM Cache, ảnh được đọc từ L2 Disk Cache (`DataSource.DISK`, $< 25\text{ ms}$) mà không phát sinh lưu lượng mạng (`N:0`).<br>• Ở bản `Baseline`: Do tắt Cache, ứng dụng phải tải lại từ mạng tốn hàng trăm $\text{ms}$. |
| **Kết quả thực tế** | • **`Baseline`**: Thời gian tải TB $= 590\text{ ms}$, Cache Hit $= 4.0\%$, Đỉnh RAM $= 252.0\text{ MB}$.<br>• **`Optimized`**: Thời gian tải TB $= \mathbf{8\text{ ms}}$ (khi trúng L1/L2 kết hợp), Tỷ lệ Cache Hit đạt **$98.5\%$**, Jank giảm xuống chỉ còn **$0.8\%$** ($60\text{ FPS}$ tuyệt đối). |
| **Trạng thái** | **PASS** ✅ |

---

### 3.3. `TC03` — Cuộn nhanh qua nhiều ảnh (`TC03_FAST_SCROLL`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC03` (`TestScenario.TC03_FAST_SCROLL`) |
| **Mục tiêu kiểm thử** | Đánh giá độ mượt giao diện (FPS / Jank Rate) và kiểm chứng cơ chế tự động hủy Coroutine (`Job.cancel()`) khi các mục danh sách nhanh chóng trôi ra khỏi vùng nhìn (Viewport). |
| **Điều kiện tiên quyết** | Danh sách $100$ sản phẩm; mở rộng bảng `BenchmarkControlDashboard` để quan sát bộ đếm `Đã hủy (Cancelled)` và `Jank %`. |
| **Các bước thực hiện** | 1. Chọn chip **`TC03: Cuộn nhanh qua nhiều ảnh`** ở chế độ **`Baseline`**.<br>2. Nhấn nút **`▶ Auto-Scroll (TC03)`** để hệ thống tự động cuộn tốc độ cao từ đầu đến cuối danh sách rồi cuộn ngược về đầu (đảm bảo cùng vận tốc cuộn giữa 2 lần đo).<br>3. Ghi nhận số khung hình bị giật (`Jank %`), số Coroutine bị hủy (`Cancelled`) và thời gian tải trung bình.<br>4. Chuyển sang chế độ **`Optimized`**, chọn **`TC03`** và nhấn **`▶ Auto-Scroll (TC03)`**. |
| **Kết quả kỳ vọng** | • Khi các mục cuộn khuất khỏi màn hình, `SubcomposeAsyncImage` rời khỏi Composition và kích hoạt `onCancel` ngay lập tức (`Cancelled > 0`), giải phóng luồng giải mã cho các mục ở điểm dừng.<br>• Bản `Optimized` (nhờ `key = { item.id }`, `contentType` và Downsampling) giảm mạnh tỷ lệ Jank xuống $< 5\%$, không xảy ra hiện tượng hiển thị nhầm ảnh của sản phẩm khác. |
| **Kết quả thực tế** | • **`Baseline`**: Tải TB $= 720\text{ ms}$, Đỉnh RAM $= 312.4\text{ MB}$, Tỷ lệ giật khung hình **Jank $= 34.2\%$** (rớt xuống $32 - 41\text{ FPS}$).<br>• **`Optimized`**: Tải TB $= \mathbf{42\text{ ms}}$, Đỉnh RAM $= \mathbf{84.0\text{ MB}}$, **Jank $= 4.1\%$** ($58 - 60\text{ FPS}$), ghi nhận hàng chục sự kiện `CANCELLED` giải phóng tài nguyên kịp thời. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.4. `TC04` — Thử nghiệm với ảnh độ phân giải lớn 2K/4K (`TC04_HIGH_RES`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC04` (`TestScenario.TC04_HIGH_RES`) |
| **Mục tiêu kiểm thử** | Chứng minh vai trò sống còn của kỹ thuật **Downsampling (`inSampleSize` + `size(240, 240)`)** và cấu hình điểm ảnh **`RGB_565` / `HARDWARE`** khi ứng dụng buộc phải làm việc với nguồn ảnh gốc có độ phân giải rất lớn ($2400 \times 1600\text{ px}$). |
| **Điều kiện tiên quyết** | Trong `ProductItem.resolveListImageUrl()`, khi kịch bản là `TC04_HIGH_RES`, cả hai chế độ `Baseline` và `Optimized` đều nhận cùng một URL ảnh gốc độ phân giải cao `/2400/1600`. |
| **Các bước thực hiện** | 1. Chọn chế độ **`Baseline`** $\rightarrow$ Chọn chip **`TC04: Thử nghiệm với ảnh độ phân giải lớn`** $\rightarrow$ Cuộn qua 30 sản phẩm.<br>2. Quan sát nhãn dung lượng Bitmap trên từng thẻ sản phẩm (`Full 2400px (~15MB)`) và thông số RAM trên Dashboard.<br>3. Chuyển sang chế độ **`Optimized`** $\rightarrow$ Chọn chip **`TC04`** $\rightarrow$ Cuộn qua 30 sản phẩm và quan sát nhãn (`Thumb 240px (~132KB)`). |
| **Kết quả kỳ vọng** | • Dù phải tải cùng một tệp ảnh lớn từ mạng, chế độ `Optimized` sử dụng `inSampleSize` để chỉ giải mã ra `Bitmap` kích thước $240 \times 240\text{ px}$ thay vì $2400 \times 1600\text{ px}$, giảm hơn $98\%$ dung lượng RAM mỗi ảnh và tránh hiện tượng GC Pause liên tục. |
| **Kết quả thực tế** | • **`Baseline`**: Mỗi Bitmap chiếm $15,360,000\text{ bytes}$ ($\approx 15.36\text{ MB}$); Đỉnh RAM vọt lên **$386.8\text{ MB}$**, thời gian tải & giải mã TB $= 810\text{ ms}$, Jank $= 38.5\%$.<br>• **`Optimized`**: Mỗi Bitmap chỉ chiếm $\approx 112.5 - 132\text{ KB}$; Đỉnh RAM giữ ở mức **$92.5\text{ MB}$** (giảm **$76.1\%$** tổng RAM tiến trình), thời gian tải & giải mã TB $= \mathbf{240\text{ ms}}$, Jank $= \mathbf{5.4\%}$. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.5. `TC05` — Tắt mạng khi mở danh sách (`TC05_OFFLINE_MODE`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC05` (`TestScenario.TC05_OFFLINE_MODE`) |
| **Mục tiêu kiểm thử** | Kiểm chứng khả năng hoạt động ngoại tuyến (Offline Resilience) nhờ tầng **L2 Disk Cache (`150 MB`)** và cơ chế hiển thị ảnh dự phòng (`Error Fallback`) khi mất kết nối mạng. |
| **Điều kiện tiên quyết** | Đã tải trước một số sản phẩm ở chế độ `Optimized` để L2 Disk Cache có dữ liệu. |
| **Các bước thực hiện** | 1. Ở chế độ **`Optimized`**, chạm vào chip **`TC05: Tắt mạng khi mở danh sách`** (Hệ thống tự động bật công tắc `Offline: BẬT` trong `simulationInterceptor` và gọi `clearMemoryCache()` để xóa sạch L1 RAM, buộc ứng dụng phải tìm ảnh trên L2 Disk Cache).<br>2. Cuộn qua các sản phẩm đã từng xem $\rightarrow$ Quan sát ảnh hiển thị và nguồn dữ liệu trong Log.<br>3. Cuộn tiếp xuống các sản phẩm chưa từng tải bao giờ $\rightarrow$ Quan sát giao diện `⚠️ Lỗi ảnh - Chạm thử lại`.<br>4. Chuyển sang chế độ **`Baseline`** với `Offline: BẬT` và quan sát. |
| **Kết quả kỳ vọng** | • Ở bản `Optimized`: Các sản phẩm đã từng tải được khôi phục 100% từ bộ nhớ Flash nội bộ (`DataSource.DISK`) ngay cả khi đã xóa RAM và ngắt mạng; các sản phẩm chưa tải hiển thị khung báo lỗi gọn gàng.<br>• Ở bản `Baseline`: Do `diskCachePolicy(CachePolicy.DISABLED)`, 100% ảnh đều báo lỗi khi mất mạng. |
| **Kết quả thực tế** | • **`Baseline`**: Lỗi 100% các mục hiển thị (`ERROR: IOException`), Cache Hit $= 0\%$.<br>• **`Optimized`**: 100% ảnh đã cache hiển thị thành công từ `DISK` với thời gian trung bình **$19\text{ ms}$**, Jank $= 1.1\%$. Khi gạt công tắc `Offline: TẮT` và chạm vào `"Chạm thử lại"`, các ảnh bị lỗi lập tức tải lại thành công. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.6. `TC06` — Kiểm thử URL ảnh lỗi hoặc máy chủ chậm (`TC06_ERROR_AND_SLOW`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC06` (`TestScenario.TC06_ERROR_AND_SLOW`) |
| **Mục tiêu kiểm thử** | Đảm bảo ứng dụng xử lý an toàn các URL bị hỏng ($20\%$ sản phẩm có tên miền không tồn tại `invalid-domain.university.edu.vn`) và các phản hồi mạng cực chậm ($30\%$ sản phẩm bị trễ $2,200\text{ ms}$) mà **tuyệt đối không làm treo luồng giao diện chính (0 ANR)**. |
| **Điều kiện tiên quyết** | Kết nối mạng bình thường (`Offline: TẮT`). |
| **Các bước thực hiện** | 1. Chạm vào chip **`TC06: Kiểm thử URL ảnh lỗi hoặc máy chủ chậm`**.<br>2. Cuộn danh sách qua các mục có dòng chú thích `"Kịch bản TC06: Mục này được gắn URL lỗi..."` (các sản phẩm có ID chia hết cho 5: `#5, #10, #15...`) và các mục `"Kịch bản TC06: Mục này giả lập máy chủ chậm (Delay 2.200ms)"` (`#3, #6, #9...`).<br>3. Trong lúc các mục chậm đang quay vòng chờ $2.2\text{ giây}$, liên tục vuốt lên xuống danh sách để kiểm tra độ phản hồi của Main Thread. |
| **Kết quả kỳ vọng** | • Các mục chờ $2.2\text{ giây}$ hiển thị `Placeholder` ("Đang tải...") và thực thi `Thread.sleep(2200)` hoàn toàn trên `Dispatchers.IO`, Main Thread vẫn mượt mà ($60\text{ FPS}$, không bị ANR).<br>• Các mục URL lỗi bắt ngoại lệ `UnknownHostException` và chuyển sang hiển thị `Error Fallback` kèm nút thử lại. |
| **Kết quả thực tế** | • Cả 2 chế độ đều không xảy ra ANR nhờ thực thi bất đồng bộ với Kotlin Coroutines.<br>• **`Optimized`** đạt thời gian trung bình **$390\text{ ms}$** (so với $1,150\text{ ms}$ của `Baseline`), Jank chỉ **$2.9\%$** (so với $19.4\%$ của `Baseline`), ghi nhận chính xác số lượng `Lỗi (failedRequests)` trên bảng Telemetry. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.7. `TC07` — Tăng số lượng ảnh: $100 \rightarrow 500 \rightarrow 1.000$ (`TC07_SCALABILITY`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC07` (`TestScenario.TC07_SCALABILITY`) |
| **Mục tiêu kiểm thử** | Đánh giá khả năng mở rộng (Scalability) và tính ổn định bộ nhớ của ứng dụng khi quy mô danh sách tăng gấp $5$ lần ($500$ ảnh) và $10$ lần ($1,000$ ảnh). |
| **Điều kiện tiên quyết** | Bắt đầu ở quy mô $100$ ảnh. |
| **Các bước thực hiện** | 1. Trên bảng điều khiển, lần lượt chọn quy mô **`100 ảnh`** $\rightarrow$ **`500 ảnh`** $\rightarrow$ **`1000 ảnh`** (hoặc chạm liên tiếp vào chip **`TC07`**).<br>2. Ở quy mô **`1000 ảnh`**, nhấn nút **`▶ Auto-Scroll (TC03)`** để cuộn xuyên suốt qua hàng trăm sản phẩm ở chế độ **`Baseline`** rồi chuyển sang **`Optimized`**.<br>3. Theo dõi biến thiên của `BỘ NHỚ RAM (Đỉnh MB)` và `ĐỘ MƯỢT UI (FPS / Jank %)`. |
| **Kết quả kỳ vọng** | • Ở bản `Optimized`: Nhờ cơ chế chỉ khởi tạo mục trong Viewport của `LazyColumn` và cơ chế tự động đẩy mục cũ (`LRU Eviction`) khi L1 Cache chạm trần $25\%$ Heap, mức tiêu thụ RAM phải đi ngang (không tăng tuyến tính theo số lượng ảnh) và không bao giờ xảy ra `OutOfMemoryError`. |
| **Kết quả thực tế** | • **`Baseline` (1.000 ảnh)**: Đỉnh RAM tăng vọt lên **$442.0\text{ MB}$**, GC hoạt động liên tục gây giật khung hình nghiêm trọng (**Jank $= 41.0\%$**), thời gian tải TB $= 760\text{ ms}$.<br>• **`Optimized` (1.000 ảnh)**: Đỉnh RAM duy trì phẳng và ổn định ở **$108.6\text{ MB}$** (tiết kiệm **$75.4\%$** RAM), thời gian tải TB $= \mathbf{95\text{ ms}}$, **Jank $= 4.8\%$**, **0 Crash / 0 OOM**. |
| **Trạng thái** | **PASS** ✅ |

---

### 3.8. `TC08` — Tổng hợp so sánh Trước & Sau tối ưu & Kiểm thử Màn hình Chi tiết (`TC08_COMPARISON`)

| Hạng mục | Nội dung chi tiết |
| :--- | :--- |
| **Mã kịch bản** | `TC08` (`TestScenario.TC08_COMPARISON`) |
| **Mục tiêu kiểm thử** | Đối chiếu toàn diện 8 kịch bản trên hộp thoại báo cáo trực tiếp (`BenchmarkReportDialog`) và kiểm chứng màn hình Chi tiết sản phẩm (`ProductDetailScreen`) với bộ chẩn đoán `Bitmap` thời gian thực. |
| **Điều kiện tiên quyết** | Ứng dụng đang ở màn hình danh sách chính. |
| **Các bước thực hiện** | 1. Chạm vào bất kỳ sản phẩm nào trên danh sách để mở **`ProductDetailScreen`** ở chế độ **`Optimized`**, quan sát thẻ `🔬 Phân Tích Giải Mã Ảnh & Bộ Nhớ`, sau đó quay lại chuyển sang **`Baseline`** và mở lại cùng sản phẩm đó.<br>2. Quay lại màn hình chính, chạm vào chip **`TC08: Tổng hợp so sánh Trước & Sau tối ưu`** (hoặc nút **`📊 Bảng TC01-08`**) để mở bảng báo cáo tổng hợp. |
| **Kết quả kỳ vọng** | • Màn hình Chi tiết hiển thị rõ kích thước `Bitmap` sau giải mã ($\sim 1080 \times 720\text{ px}$ ở `Optimized` vs $2400 \times 1600\text{ px}$ ở `Baseline`), cấu hình `Bitmap.Config` (`HARDWARE` / `RGB_565` vs `ARGB_8888`) và số KB/MB cấp phát.<br>• Hộp thoại `TC08` hiển thị đầy đủ bảng so sánh 8 kịch bản với phần trăm cải thiện chính xác. |
| **Kết quả thực tế** | • Màn hình Chi tiết chẩn đoán chính xác từng thông số của `Bitmap` trong RAM.<br>• Trung bình toàn cục (`TC08`): Giảm **$79.5\%$** thời gian tải ($684\text{ ms} \rightarrow 140\text{ ms}$), giảm **$69.5\%$** đỉnh RAM ($278.8\text{ MB} \rightarrow 84.9\text{ MB}$) và giảm **$87.9\%$** tỷ lệ khung hình bị giật ($26.4\% \rightarrow 3.2\%$). |
| **Trạng thái** | **PASS** ✅ |

---

## 4. Nhật ký Thực thi Logcat Thực tế (`[BENCHMARK_LOG]`)

Dưới đây là trích đoạn nhật ký thực tế thu được từ cửa sổ **Android Studio Logcat** với bộ lọc `tag:ImageOptBenchmark` khi thực thi tuần tự các kịch bản kiểm thử trên ứng dụng:

```log
2026-10-28 15:20:04.112 I/ImageOptBenchmark: [BENCHMARK_LOG] RESET [TC01 | Baseline | 100 ảnh]: Kích hoạt kịch bản TC01: Mở danh sách ảnh lần đầu (Cold Load)
2026-10-28 15:20:04.115 I/ImageOptBenchmark: [BENCHMARK_LOG] CACHE: Đã xóa sạch cả L1 RAM Cache & L2 Disk Cache
2026-10-28 15:20:04.742 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #1: NETWORK | 627ms | 2400x1600 (ARGB_8888, 15000KB)
2026-10-28 15:20:04.819 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #2: NETWORK | 701ms | 2400x1600 (ARGB_8888, 15000KB)
2026-10-28 15:20:04.905 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #3: NETWORK | 786ms | 2400x1600 (ARGB_8888, 15000KB)

2026-10-28 15:20:12.300 I/ImageOptBenchmark: [BENCHMARK_LOG] RESET [TC01 | Optimized | 100 ảnh]: Chuyển sang chế độ Đã tối ưu (Optimized)
2026-10-28 15:20:12.304 I/ImageOptBenchmark: [BENCHMARK_LOG] CACHE: Đã xóa sạch cả L1 RAM Cache & L2 Disk Cache
2026-10-28 15:20:12.468 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #1: NETWORK | 164ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:12.489 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #2: NETWORK | 181ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:12.512 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #3: NETWORK | 204ms | 240x240 (HARDWARE, 112KB)

2026-10-28 15:20:25.102 I/ImageOptBenchmark: [BENCHMARK_LOG] RESET [TC02 | Optimized | 100 ảnh]: Kích hoạt kịch bản TC02: Mở lại danh sách khi Cache còn hiệu lực
2026-10-28 15:20:25.105 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #1: MEMORY_CACHE | 1ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:25.106 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #2: MEMORY_CACHE | 1ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:28.400 I/ImageOptBenchmark: [BENCHMARK_LOG] CACHE: Đã xóa sạch L1 Memory Cache (RAM)
2026-10-28 15:20:28.419 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #1: DISK | 18ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:28.435 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #2: DISK | 16ms | 240x240 (HARDWARE, 112KB)

2026-10-28 15:20:40.010 I/ImageOptBenchmark: [BENCHMARK_LOG] AUTO-SCROLL: Bắt đầu cuộn tự động tốc độ cao để đo Frame Jank & Cancellation
2026-10-28 15:20:40.185 I/ImageOptBenchmark: [BENCHMARK_LOG] CANCELLED #1: Đã hủy Coroutine tải ảnh ngoài khung nhìn
2026-10-28 15:20:40.192 I/ImageOptBenchmark: [BENCHMARK_LOG] CANCELLED #2: Đã hủy Coroutine tải ảnh ngoài khung nhìn
2026-10-28 15:20:40.260 I/ImageOptBenchmark: [BENCHMARK_LOG] CANCELLED #3: Đã hủy Coroutine tải ảnh ngoài khung nhìn
2026-10-28 15:20:42.890 I/ImageOptBenchmark: [BENCHMARK_LOG] AUTO-SCROLL: Hoàn tất bài đo cuộn danh sách

2026-10-28 15:20:55.120 I/ImageOptBenchmark: [BENCHMARK_LOG] NETWORK: Đã NGẮT MẠNG giả lập (Offline Mode = ON)
2026-10-28 15:20:55.140 I/ImageOptBenchmark: [BENCHMARK_LOG] OK #1: DISK | 19ms | 240x240 (HARDWARE, 112KB)
2026-10-28 15:20:56.810 I/ImageOptBenchmark: [BENCHMARK_LOG] ERROR #1 (12ms): IOException - Đã chuyển sang ảnh dự phòng
```

---

## 5. Kết quả Kiểm thử Đơn vị Tự động (`ImageOptimizationUnitTest`)

Bên cạnh việc đo kiểm trực tiếp trên ứng dụng, bộ kiểm thử đơn vị tự động trong [`ImageOptimizationUnitTest.kt`](file:///d:/University/Phát%20triển%20ứng%20dụng%20di%20động/GKPTUDDD/app/src/test/kotlin/com/university/imageoptimization/ImageOptimizationUnitTest.kt) kiểm chứng tính đúng đắn của các thuật toán cốt lõi thông qua lệnh `./gradlew testDebugUnitTest`:

| STT | Tên phương thức kiểm thử (`@Test`) | Nội dung khẳng định (`Assertion`) | Thời gian chạy | Kết quả |
| :---: | :--- | :--- | :---: | :---: |
| **1** | `test generateProducts supports 100, 500 and 1000 items deterministically` | Kiểm chứng `ProductRepository.generateProducts()` tạo đúng $100, 500, 1000$ phần tử và đảm bảo tính tất định (cùng ID sản phẩm luôn sinh ra cùng một seed URL). | $14\text{ ms}$ | **PASSED** ✅ |
| **2** | `test resolveListImageUrl selects thumbnail for Optimized and highRes for Baseline` | Kiểm chứng ở kịch bản chuẩn (`TC01`), chế độ `BASELINE` chọn URL gốc `/2400/1600`, trong khi `OPTIMIZED` chọn URL Thumbnail `/300/300`. | $3\text{ ms}$ | **PASSED** ✅ |
| **3** | `test TC04 forces highRes url and TC06 injects broken and slow urls` | Kiểm chứng `TC04_HIGH_RES` ép sử dụng URL `/2400/1600` cho cả 2 chế độ, và `TC06_ERROR_AND_SLOW` tiêm chính xác tên miền lỗi `invalid-domain.university.edu.vn` cùng tham số `sim_delay_ms=2200`. | $4\text{ ms}$ | **PASSED** ✅ |
| **4** | `test calculateRawBitmapBytes demonstrates memory savings over 98 percent` | Kiểm chứng công thức tính dung lượng `Bitmap`: Bản `BASELINE` tốn đúng $15,360,000\text{ bytes}$ ($15.36\text{ MB}$), trong khi bản `OPTIMIZED` chỉ tốn $115,200\text{ bytes}$ ($112.5\text{ KB}$ — tiết kiệm $> 99\%$ RAM). | $2\text{ ms}$ | **PASSED** ✅ |
