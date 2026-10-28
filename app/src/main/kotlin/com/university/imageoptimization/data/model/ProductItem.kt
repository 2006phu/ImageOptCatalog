package com.university.imageoptimization.data.model

import androidx.compose.runtime.Immutable
import com.university.imageoptimization.image.OptimizationMode
import com.university.imageoptimization.image.TestScenario

/**
 * Mô hình dữ liệu Sản phẩm được đánh dấu [@Immutable] giúp Jetpack Compose
 * bỏ qua (skip) việc Recomposition không cần thiết khi cuộn danh sách (Chủ đề 7).
 */
@Immutable
data class ProductItem(
    val id: Int,
    val code: String,
    val name: String,
    val category: String,
    val priceVnd: Long,
    val rating: Float,
    val reviewCount: Int,
    val description: String,
    val thumbnailUrl: String,
    val detailImageUrl: String,
    val highResImageUrl: String,
    val brokenImageUrl: String,
    val thumbnailWidthPx: Int = 300,
    val thumbnailHeightPx: Int = 300,
    val highResWidthPx: Int = 2400,
    val highResHeightPx: Int = 1600,
    val isSimulatedErrorItem: Boolean = false,
    val isSimulatedSlowItem: Boolean = false
) {
    /**
     * Chọn URL ảnh cho màn hình danh sách (List View) tùy theo Chế độ tối ưu và Kịch bản thực nghiệm:
     * - Nếu kịch bản là [TestScenario.TC06_ERROR_AND_SLOW] và item được đánh dấu lỗi -> trả về URL lỗi.
     * - Nếu kịch bản là [TestScenario.TC04_HIGH_RES] hoặc đang ở chế độ [OptimizationMode.BASELINE] ->
     *   tải ảnh gốc độ phân giải lớn (2400x1600) để bộc lộ chi phí mạng & giải mã khi không dùng Thumbnail.
     * - Nếu đang ở chế độ [OptimizationMode.OPTIMIZED] (kịch bản thông thường) -> tải ảnh Thumbnail (300x300)
     *   vừa khít với khung hiển thị trong danh sách (Yêu cầu 4).
     */
    fun resolveListImageUrl(
        mode: OptimizationMode,
        scenario: TestScenario,
        cacheBusterToken: Int
    ): String {
        if (scenario == TestScenario.TC06_ERROR_AND_SLOW && isSimulatedErrorItem) {
            return "$brokenImageUrl?cb=$cacheBusterToken"
        }

        val baseUrl = when {
            scenario == TestScenario.TC04_HIGH_RES -> highResImageUrl
            mode == OptimizationMode.BASELINE -> highResImageUrl
            else -> thumbnailUrl
        }

        val slowParam = if (scenario == TestScenario.TC06_ERROR_AND_SLOW && isSimulatedSlowItem) {
            "&sim_delay_ms=2200"
        } else {
            ""
        }

        return "$baseUrl?cb=$cacheBusterToken&mode=${mode.name.lowercase()}$slowParam"
    }

    /**
     * Chọn URL ảnh cho màn hình chi tiết (Detail Screen):
     * Luôn hiển thị ảnh chất lượng cao (1200x800 hoặc 2400x1600).
     */
    fun resolveDetailImageUrl(
        mode: OptimizationMode,
        cacheBusterToken: Int
    ): String {
        val baseUrl = if (mode == OptimizationMode.BASELINE) highResImageUrl else detailImageUrl
        return "$baseUrl?cb=$cacheBusterToken&detail=true"
    }

    /**
     * Tính toán lý thuyết dung lượng bộ nhớ Bitmap khi giải mã không nén:
     * Memory (Bytes) = Width * Height * BytesPerPixel
     */
    fun calculateRawBitmapBytes(mode: OptimizationMode, scenario: TestScenario): Long {
        return if (mode == OptimizationMode.BASELINE) {
            // Baseline giải mã nguyên bản 2400x1600 với ARGB_8888 (4 bytes/pixel) -> ~15.36 MB / ảnh!
            highResWidthPx.toLong() * highResHeightPx.toLong() * 4L
        } else {
            // Optimized downsample về kích thước hiển thị thực tế (~240x240 px) với RGB_565/Hardware (~2-4 bytes/pixel) -> ~115 KB - 230 KB / ảnh!
            val targetPx = if (scenario == TestScenario.TC04_HIGH_RES) 260L else 240L
            targetPx * targetPx * 2L
        }
    }
}
