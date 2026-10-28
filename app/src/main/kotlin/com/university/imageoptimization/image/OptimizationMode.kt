package com.university.imageoptimization.image

/**
 * Hai chế độ vận hành của ứng dụng để phục vụ đối chứng thực nghiệm (Deliverable D5 & Yêu cầu 6):
 * - [BASELINE]: Phiên bản cơ sở chưa tối ưu (Tải ảnh gốc độ phân giải cao, giải mã ARGB_8888 nguyên bản,
 *   không dùng Downsampling, giới hạn/tắt cache, không tối ưu key của LazyColumn).
 * - [OPTIMIZED]: Phiên bản đã tối ưu toàn diện (Phân tách Thumbnail/Full-res, Downsampling theo kích thước View,
 *   Memory Cache Lru 25% RAM + WeakCache, Disk Cache 150MB, Hardware/RGB_565 Bitmap, LazyColumn key & contentType).
 */
enum class OptimizationMode(
    val label: String,
    val shortLabel: String,
    val description: String,
    val memoryCacheSummary: String,
    val diskCacheSummary: String,
    val decodeStrategySummary: String
) {
    BASELINE(
        label = "Chưa tối ưu (Baseline)",
        shortLabel = "Baseline",
        description = "Tải ảnh nguyên bản (Full-Res), giải mã ARGB_8888 (4 bytes/pixel) với Size.ORIGINAL, tắt Disk Cache và không gắn key cố định cho danh sách cuộn.",
        memoryCacheSummary = "Tắt / Tối thiểu (Không lưu LruCache hiệu quả)",
        diskCacheSummary = "DISABLED (Luôn tải lại từ mạng khi mở lại)",
        decodeStrategySummary = "Size.ORIGINAL + ARGB_8888 (Không Downsample)"
    ),
    OPTIMIZED(
        label = "Đã tối ưu (Optimized)",
        shortLabel = "Optimized",
        description = "Tải Thumbnail cho danh sách & Full-Res cho chi tiết, Downsampling khớp kích thước khung nhìn, bật L1 RAM Cache (25%) + L2 Disk Cache (150MB) + Hardware Bitmap.",
        memoryCacheSummary = "L1 LruCache (25% Heap) + WeakMemoryCache",
        diskCacheSummary = "L2 DiskLruCache (150 MB trên bộ nhớ trong)",
        decodeStrategySummary = "Downsampling theo View + Hardware/RGB_565"
    );

    companion object {
        fun fromString(value: String?): OptimizationMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPTIMIZED
        }
    }
}
