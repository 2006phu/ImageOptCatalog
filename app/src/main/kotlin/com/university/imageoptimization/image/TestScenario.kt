package com.university.imageoptimization.image

/**
 * 8 Kịch bản thực nghiệm bắt buộc theo Mục 3 của đề bài (TC01 -> TC08).
 */
enum class TestScenario(
    val code: String,
    val title: String,
    val description: String,
    val metricsToObserve: String,
    val recommendedItemCount: Int = 100
) {
    TC01_FIRST_LOAD(
        code = "TC01",
        title = "Mở danh sách ảnh lần đầu (Cold Load)",
        description = "Xóa toàn bộ RAM & Disk Cache trước khi tải danh sách ảnh từ mạng lần đầu tiên.",
        metricsToObserve = "Thời gian hiển thị loạt ảnh đầu, thời gian tải trung bình (ms) và mức RAM tiêu thụ (MB)."
    ),
    TC02_WARM_CACHE(
        code = "TC02",
        title = "Mở lại danh sách khi Cache còn hiệu lực",
        description = "Đóng và mở lại màn hình danh sách khi ảnh đã được lưu trong L1 Memory Cache hoặc L2 Disk Cache.",
        metricsToObserve = "Thời gian hiển thị (ms) giảm mạnh so với TC01 và tỷ lệ Cache Hit đạt 95-100%."
    ),
    TC03_FAST_SCROLL(
        code = "TC03",
        title = "Cuộn nhanh qua nhiều ảnh (Fast Fling)",
        description = "Cuộn tự động tốc độ cao qua toàn bộ danh sách để kiểm tra hiện tượng giật khung hình (Jank) và cơ chế hủy Coroutine.",
        metricsToObserve = "Số khung hình bị giật (Janky Frames), số request được hủy kịp thời (Cancelled) và ảnh hiển thị đúng vị trí."
    ),
    TC04_HIGH_RES(
        code = "TC04",
        title = "Thử nghiệm với ảnh độ phân giải lớn (4K/2K)",
        description = "Sử dụng toàn bộ URL ảnh độ phân giải cao (2400x1600 px) cho danh sách để đánh giá tác động của Downsampling.",
        metricsToObserve = "Bộ nhớ Native/Java Heap (MB), thời gian giải mã (Decode ms) và nguy cơ OutOfMemoryError."
    ),
    TC05_OFFLINE_MODE(
        code = "TC05",
        title = "Tắt mạng khi mở danh sách (Offline Mode)",
        description = "Ngắt kết nối mạng (giả lập Offline Interceptor) sau khi đã cache một phần danh sách.",
        metricsToObserve = "Ảnh đã cache vẫn hiển thị tức thì từ Disk/RAM; ảnh chưa cache hiển thị ảnh lỗi dự phòng (Fallback) mượt mà."
    ),
    TC06_ERROR_AND_SLOW(
        code = "TC06",
        title = "Kiểm thử URL ảnh lỗi hoặc máy chủ chậm",
        description = "Trộn 20% URL ảnh bị hỏng (HTTP 404/Invalid host) và 30% URL phản hồi chậm (delay 2.500ms).",
        metricsToObserve = "Giao diện Main Thread không bị treo (0 ANR), hiển thị Placeholder khi chờ và Error Fallback kèm nút Thử lại."
    ),
    TC07_SCALABILITY(
        code = "TC07",
        title = "Tăng số lượng ảnh: 100 -> 500 -> 1.000",
        description = "Kiểm thử khả năng mở rộng quy mô danh sách sản phẩm từ 100 lên 500 và 1.000 ảnh.",
        metricsToObserve = "Độ ổn định của bộ nhớ (nhờ cơ chế thu hồi LruCache & tái sử dụng LazyColumn) và độ trễ giao diện."
    ),
    TC08_COMPARISON(
        code = "TC08",
        title = "Tổng hợp so sánh Trước & Sau tối ưu",
        description = "Đối chiếu toàn diện các chỉ số giữa phiên bản Cơ sở (Baseline) và phiên bản Đã tối ưu (Optimized).",
        metricsToObserve = "Phần trăm cải thiện thời gian tải, giảm đỉnh bộ nhớ RAM, giảm tỷ lệ Jank và phân tích đánh đổi (Trade-offs)."
    )
}
