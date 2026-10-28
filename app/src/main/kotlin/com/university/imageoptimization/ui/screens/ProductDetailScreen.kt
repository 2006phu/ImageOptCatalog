package com.university.imageoptimization.ui.screens

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.university.imageoptimization.data.model.ProductItem
import com.university.imageoptimization.image.OptimizationMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Chi tiết Sản phẩm (Đáp ứng Yêu cầu 1 & Yêu cầu 4):
 * - Tải ảnh kích thước lớn (1200x800 ở chế độ Optimized hoặc 2400x1600 ở chế độ Baseline).
 * - Phân tích trực tiếp thông số kỹ thuật của đối tượng [Bitmap] sau khi giải mã:
 *   + Độ phân giải thực tế trong RAM (Width x Height px)
 *   + Cấu hình điểm ảnh (`ARGB_8888`, `RGB_565`, `HARDWARE`)
 *   + Dung lượng bộ nhớ RAM đã cấp phát (`allocationByteCount`)
 *   + Nguồn dữ liệu (`MEMORY_CACHE`, `DISK`, `NETWORK`) và thời gian tải (ms).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductItem,
    mode: OptimizationMode,
    cacheBusterToken: Int,
    imageLoader: ImageLoader,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var retryHash by remember { mutableStateOf(0) }
    val requestStartMs = remember(product.id, mode, retryHash) { SystemClock.elapsedRealtime() }
    var loadDurationMs by remember { mutableLongStateOf(0L) }
    var decodedWidth by remember { mutableStateOf(0) }
    var decodedHeight by remember { mutableStateOf(0) }
    var bitmapConfigName by remember { mutableStateOf("Đang giải mã...") }
    var allocatedBytes by remember { mutableLongStateOf(0L) }
    var dataSourceName by remember { mutableStateOf("Đang kiểm tra...") }

    val detailUrl = remember(product.id, mode, cacheBusterToken, retryHash) {
        "${product.resolveDetailImageUrl(mode, cacheBusterToken)}&retry=$retryHash"
    }

    val imageRequest = remember(detailUrl, mode) {
        ImageRequest.Builder(context)
            .data(detailUrl)
            .apply {
                if (mode == OptimizationMode.BASELINE) {
                    size(Size.ORIGINAL)
                    precision(Precision.EXACT)
                    allowHardware(false)
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                } else {
                    // Giải mã vừa khít với khung hiển thị chi tiết (~1080x720 px)
                    size(1080, 720)
                    precision(Precision.INEXACT)
                    crossfade(200)
                }
            }
            .listener(
                onSuccess = { _, result ->
                    loadDurationMs = (SystemClock.elapsedRealtime() - requestStartMs).coerceAtLeast(1L)
                    dataSourceName = result.dataSource.name
                    val bmp = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bmp != null) {
                        decodedWidth = bmp.width
                        decodedHeight = bmp.height
                        bitmapConfigName = bmp.config?.name ?: "HARDWARE"
                        allocatedBytes = bmp.allocationByteCount.toLong()
                    }
                },
                onError = { _, _ ->
                    loadDurationMs = (SystemClock.elapsedRealtime() - requestStartMs).coerceAtLeast(1L)
                    dataSourceName = "ERROR (Failed)"
                }
            )
            .build()
    }

    val currencyFormat = remember { NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chi tiết sản phẩm • ${product.code}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chế độ: ${mode.label}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("← Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Khung hiển thị ảnh lớn (Full/Detail Image)
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        imageLoader = imageLoader,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Đang tải & giải mã ảnh độ phân giải cao...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        },
                        error = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "⚠️ Không thể tải ảnh chi tiết (Mất mạng hoặc URL lỗi)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB91C1C)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { retryHash++ }) {
                                    Text("Thử tải lại (Retry)")
                                }
                            }
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )
                }
            }

            // Thông tin sản phẩm
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = product.category.uppercase(Locale.getDefault()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${currencyFormat.format(product.priceVnd)} ₫",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD84315)
                        )
                        Text(
                            text = "★ ${String.format(Locale.US, "%.1f", product.rating)} (${product.reviewCount} đánh giá)",
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                }
            }

            // Phân tích kỹ thuật giải mã Bitmap & Bộ nhớ (Yêu cầu 4 & 5)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔬 Phân Tích Giải Mã Ảnh & Bộ Nhớ (Real-time Diagnostics)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DiagnosticItem("URL yêu cầu:", detailUrl)
                    DiagnosticItem("Nguồn dữ liệu (DataSource):", dataSourceName)
                    DiagnosticItem("Thời gian phản hồi & giải mã:", "$loadDurationMs ms")
                    DiagnosticItem(
                        "Kích thước Bitmap sau giải mã:",
                        if (decodedWidth > 0) "${decodedWidth} x ${decodedHeight} px" else "Chưa có"
                    )
                    DiagnosticItem("Cấu hình Pixel (Bitmap.Config):", bitmapConfigName)
                    DiagnosticItem(
                        "Bộ nhớ Bitmap cấp phát trong RAM:",
                        if (allocatedBytes > 0) {
                            String.format(
                                Locale.US,
                                "%,d KB (%.2f MB)",
                                allocatedBytes / 1024,
                                allocatedBytes / (1024f * 1024f)
                            )
                        } else "0 KB"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val raw4kMb = (2400L * 1600L * 4L) / (1024f * 1024f)
                    val listThumbKb = (240L * 240L * 2L) / 1024L
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Giải thích tối ưu (Yêu cầu 4):\n" +
                                "• Nếu giải mã ảnh gốc 2400x1600 với ARGB_8888 cho mỗi item trên danh sách, 1 ảnh chiếm tới ${String.format(Locale.US, "%.2f", raw4kMb)} MB RAM (100 ảnh = ~1.53 GB -> Gây GC liên tục hoặc OutOfMemoryError).\n" +
                                "• Khi tách Thumbnail (300x300) và Downsample về khung nhìn 240x240 px (RGB_565), mỗi ảnh danh sách chỉ tốn ~$listThumbKb KB RAM (giảm hơn 98% bộ nhớ), và chỉ tải ảnh lớn khi người dùng thực sự mở màn hình Chi tiết này.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
    }
}
