package com.university.imageoptimization.data.repository

import com.university.imageoptimization.data.model.ProductItem
import java.util.Locale

/**
 * Nguồn tạo dữ liệu sản phẩm chuẩn hóa phục vụ thực nghiệm với quy mô:
 * 100, 500 và 1.000 sản phẩm (Đáp ứng Yêu cầu 1 & Yêu cầu 6 - TC07).
 *
 * Sử dụng dịch vụ ảnh công khai ổn định `picsum.photos` với `seed` cố định theo ID sản phẩm,
 * đảm bảo hai phiên bản Baseline và Optimized luôn tải cùng một bộ ảnh giống hệt nhau.
 */
object ProductRepository {

    private val categories = listOf(
        "Điện thoại & Tablet",
        "Laptop & Thiết bị IT",
        "Máy ảnh & Quay phim",
        "Âm thanh Hi-Res",
        "Đồng hồ thông minh",
        "Phụ kiện Gaming",
        "Gia dụng thông minh",
        "Thiết bị mạng"
    )

    private val productPrefixes = listOf(
        "Smartphone Flagship Pro",
        "Ultrabook Studio OLED",
        "Máy ảnh Mirrorless 4K",
        "Tai nghe Chống ồn ANC",
        "Smartwatch Titanium GPS",
        "Bàn phím Cơ Wireless RGB",
        "Màn hình Đồ họa 144Hz",
        "Router Wi-Fi 7 Mesh",
        "Loa Bluetooth Hi-Fi",
        "Ổ cứng SSD NVMe Gen5"
    )

    /**
     * Sinh danh sách [count] sản phẩm với dữ liệu tất định (deterministic).
     * @param count Số lượng sản phẩm cần tạo (100, 500, 1000).
     */
    fun generateProducts(count: Int): List<ProductItem> {
        val safeCount = count.coerceIn(10, 2000)
        return List(safeCount) { index ->
            val id = index + 1
            val category = categories[index % categories.size]
            val prefix = productPrefixes[index % productPrefixes.size]
            val modelNumber = 100 + (index * 7) % 900
            val priceVnd = 1_250_000L + ((index * 37L) % 45L) * 650_000L
            val rating = 3.8f + ((index * 3) % 13) * 0.1f
            val reviewCount = 15 + (index * 29) % 1200

            // Sử dụng seed cố định để ảnh thứ i luôn trả về đúng bức ảnh đó qua mọi lần đo
            val imageSeed = "android_opt_item_$id"

            // 1. Thumbnail (300x300 px) ~ 15-25 KB file, dùng cho danh sách ở bản Optimized
            val thumbnailUrl = "https://picsum.photos/seed/$imageSeed/300/300"

            // 2. Detail Image (1200x800 px) ~ 120-200 KB file, dùng cho màn hình chi tiết
            val detailImageUrl = "https://picsum.photos/seed/$imageSeed/1200/800"

            // 3. High-Res Image (2400x1600 px) ~ 500-900 KB file (~15.36 MB raw Bitmap ARGB_8888 khi giải mã!)
            val highResImageUrl = "https://picsum.photos/seed/$imageSeed/2400/1600"

            // 4. Broken URL để kiểm thử kịch bản TC06 (Cứ mỗi 5 item có 1 item lỗi = 20%)
            val brokenImageUrl = "https://invalid-domain.university.edu.vn/broken_image_$id.jpg"
            val isErrorItem = (id % 5 == 0)
            // Cứ mỗi 3 item (không lỗi) thì có 1 item giả lập server phản hồi chậm (TC06)
            val isSlowItem = !isErrorItem && (id % 3 == 0)

            ProductItem(
                id = id,
                code = String.format(Locale.US, "SP-%04d", id),
                name = "$prefix X$modelNumber (#$id)",
                category = category,
                priceVnd = priceVnd,
                rating = rating.coerceAtMost(5.0f),
                reviewCount = reviewCount,
                description = "Sản phẩm minh họa #$id thuộc danh mục $category. " +
                    "Được thiết kế để kiểm thử cơ chế tải ảnh bất đồng bộ với Kotlin Coroutines & Coil, " +
                    "đánh giá hiệu quả của bộ nhớ đệm 3 tầng (RAM LruCache, Disk Cache, HTTP Cache) " +
                    "và kỹ thuật Downsampling giảm kích thước giải mã Bitmap từ 2400x1600 px xuống đúng kích thước hiển thị.",
                thumbnailUrl = thumbnailUrl,
                detailImageUrl = detailImageUrl,
                highResImageUrl = highResImageUrl,
                brokenImageUrl = brokenImageUrl,
                thumbnailWidthPx = 300,
                thumbnailHeightPx = 300,
                highResWidthPx = 2400,
                highResHeightPx = 1600,
                isSimulatedErrorItem = isErrorItem,
                isSimulatedSlowItem = isSlowItem
            )
        }
    }
}
