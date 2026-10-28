package com.university.imageoptimization.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.university.imageoptimization.data.model.ProductItem
import com.university.imageoptimization.image.OptimizationMode
import com.university.imageoptimization.image.TestScenario
import com.university.imageoptimization.telemetry.LivePerformanceMetrics
import com.university.imageoptimization.ui.theme.BaselineOrange
import com.university.imageoptimization.ui.theme.OptimizedGreen
import com.university.imageoptimization.ui.theme.SurfaceDarkCard
import com.university.imageoptimization.ui.viewmodel.CatalogUiState
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    uiState: CatalogUiState,
    liveMetrics: LivePerformanceMetrics,
    imageLoader: ImageLoader,
    onSelectMode: (OptimizationMode) -> Unit,
    onSelectItemCount: (Int) -> Unit,
    onSelectScenario: (TestScenario) -> Unit,
    onToggleOffline: (Boolean) -> Unit,
    onClearRamCache: () -> Unit,
    onClearAllCaches: () -> Unit,
    onStartAutoScroll: (Boolean) -> Unit,
    onToggleDashboard: () -> Unit,
    onShowReportDialog: () -> Unit,
    onRetryFailed: () -> Unit,
    onProductClick: (ProductItem) -> Unit
) {
    val listState = rememberLazyListState()

    // Kịch bản tự động cuộn nhanh (Auto-Scroll Benchmark cho TC03 & TC07)
    LaunchedEffect(uiState.isAutoScrolling, uiState.products.size) {
        if (uiState.isAutoScrolling && uiState.products.isNotEmpty()) {
            val total = uiState.products.size
            val step = 8
            var currentIndex = listState.firstVisibleItemIndex
            while (currentIndex < total - 1 && uiState.isAutoScrolling) {
                currentIndex = (currentIndex + step).coerceAtMost(total - 1)
                listState.animateScrollToItem(currentIndex)
                delay(70L)
            }
            // Cuộn ngược lại về đầu để kiểm tra tỷ lệ Cache Hit (TC02 / TC03)
            delay(250L)
            while (currentIndex > 0 && uiState.isAutoScrolling) {
                currentIndex = (currentIndex - step * 2).coerceAtLeast(0)
                listState.animateScrollToItem(currentIndex)
                delay(60L)
            }
            onStartAutoScroll(false)
        }
    }

    val modeColor = if (uiState.mode == OptimizationMode.OPTIMIZED) OptimizedGreen else BaselineOrange

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Đề tài 02: Tối Ưu Tải & Cache Ảnh",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.mode.label} • ${uiState.itemCount} ảnh • ${uiState.scenario.code}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onToggleDashboard,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (uiState.isDashboardExpanded) "Thu gọn đo" else "Mở bộ đo",
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = modeColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Bảng điều khiển Thực nghiệm & Telemetry Thời gian thực
            if (uiState.isDashboardExpanded) {
                BenchmarkControlDashboard(
                    uiState = uiState,
                    liveMetrics = liveMetrics,
                    onSelectMode = onSelectMode,
                    onSelectItemCount = onSelectItemCount,
                    onSelectScenario = onSelectScenario,
                    onToggleOffline = onToggleOffline,
                    onClearRamCache = onClearRamCache,
                    onClearAllCaches = onClearAllCaches,
                    onStartAutoScroll = onStartAutoScroll,
                    onShowReportDialog = onShowReportDialog
                )
            } else {
                CompactTelemetryBar(
                    liveMetrics = liveMetrics,
                    onShowReportDialog = onShowReportDialog
                )
            }

            // Danh sách cuộn LazyColumn (Yêu cầu 1, 2, 6, 7)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.mode == OptimizationMode.OPTIMIZED) {
                    // PHIÊN BẢN TỐI ƯU: Cung cấp `key` cố định và `contentType` để tránh Recomposition thừa
                    items(
                        items = uiState.products,
                        key = { item -> item.id },
                        contentType = { "optimized_product_card" }
                    ) { product ->
                        ProductListItemCard(
                            product = product,
                            mode = uiState.mode,
                            scenario = uiState.scenario,
                            cacheBusterToken = uiState.cacheBusterToken,
                            retryCounter = uiState.retryCounter,
                            imageLoader = imageLoader,
                            onRetry = onRetryFailed,
                            onClick = { onProductClick(product) }
                        )
                    }
                } else {
                    // PHIÊN BẢN CƠ SỞ (BASELINE): Không gắn key ổn định, tải ảnh gốc không downsample
                    items(items = uiState.products) { product ->
                        ProductListItemCard(
                            product = product,
                            mode = uiState.mode,
                            scenario = uiState.scenario,
                            cacheBusterToken = uiState.cacheBusterToken,
                            retryCounter = uiState.retryCounter,
                            imageLoader = imageLoader,
                            onRetry = onRetryFailed,
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTelemetryBar(
    liveMetrics: LivePerformanceMetrics,
    onShowReportDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDarkCard)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RAM: ${liveMetrics.totalRamUsedMb}MB (Đỉnh ${liveMetrics.peakRamUsedMb}MB) | " +
                "Tải TB: ${liveMetrics.avgLoadTimeMs}ms | Hit: ${liveMetrics.cacheHitRatioPercent.toInt()}% | " +
                "${liveMetrics.currentFps} FPS (Jank ${liveMetrics.jankPercent}%)",
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Báo cáo",
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onShowReportDialog() }
        )
    }
}

@Composable
private fun BenchmarkControlDashboard(
    uiState: CatalogUiState,
    liveMetrics: LivePerformanceMetrics,
    onSelectMode: (OptimizationMode) -> Unit,
    onSelectItemCount: (Int) -> Unit,
    onSelectScenario: (TestScenario) -> Unit,
    onToggleOffline: (Boolean) -> Unit,
    onClearRamCache: () -> Unit,
    onClearAllCaches: () -> Unit,
    onStartAutoScroll: (Boolean) -> Unit,
    onShowReportDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDarkCard)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 4 Ô chỉ số Telemetry thời gian thực (RAM, Load Time, Cache Hit, Frame Jank)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TelemetryStatBox(
                title = "BỘ NHỚ RAM",
                primaryValue = "${liveMetrics.totalRamUsedMb} MB",
                subValue = "Đỉnh: ${liveMetrics.peakRamUsedMb} MB",
                accentColor = if (liveMetrics.totalRamUsedMb > 220f) Color(0xFFF87171) else Color(0xFF4ADE80),
                modifier = Modifier.weight(1f)
            )
            TelemetryStatBox(
                title = "THỜI GIAN TẢI",
                primaryValue = "${liveMetrics.avgLoadTimeMs} ms",
                subValue = "Decode: ${liveMetrics.avgDecodeTimeMs} ms",
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            TelemetryStatBox(
                title = "CACHE HIT (L1/L2)",
                primaryValue = "${liveMetrics.cacheHitRatioPercent.toInt()}%",
                subValue = "R:${liveMetrics.memoryCacheHits} D:${liveMetrics.diskCacheHits} N:${liveMetrics.networkLoads}",
                accentColor = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
            TelemetryStatBox(
                title = "ĐỘ MƯỢT UI",
                primaryValue = "${liveMetrics.currentFps} FPS",
                subValue = "Jank: ${liveMetrics.jankPercent}% (${liveMetrics.jankyFrames})",
                accentColor = if (liveMetrics.jankPercent > 15f) Color(0xFFF87171) else Color(0xFF4ADE80),
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Chuyển đổi Phiên bản Cơ sở (Baseline) vs Đã tối ưu (Optimized) (Deliverable D5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OptimizationMode.entries.forEach { mode ->
                val selected = uiState.mode == mode
                val bgColor = when {
                    selected && mode == OptimizationMode.OPTIMIZED -> OptimizedGreen
                    selected && mode == OptimizationMode.BASELINE -> BaselineOrange
                    else -> Color(0xFF334155)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 7.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selected) "✓ ${mode.label}" else mode.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // 3. Chọn số lượng ảnh (100 / 500 / 1.000 - TC07) & Công tắc Offline (TC05)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quy mô:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
                listOf(100, 500, 1000).forEach { count ->
                    val isSelected = uiState.itemCount == count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF334155))
                            .clickable { onSelectItemCount(count) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$count ảnh",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (uiState.isOfflineSimulated) "Offline: BẬT" else "Offline: TẮT",
                    color = if (uiState.isOfflineSimulated) Color(0xFFF87171) else Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = uiState.isOfflineSimulated,
                    onCheckedChange = onToggleOffline
                )
            }
        }

        // 4. Hàng chọn nhanh 8 Kịch bản thực nghiệm bắt buộc (TC01 -> TC08)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TestScenario.entries.forEach { scenario ->
                val selected = uiState.scenario == scenario
                FilterChip(
                    selected = selected,
                    onClick = { onSelectScenario(scenario) },
                    label = {
                        Text(
                            text = "${scenario.code}: ${scenario.title.substringBefore(" (")}",
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF334155),
                        labelColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // Mô tả kịch bản đang chọn + Số lượng Coroutines đã hủy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "📌 ${uiState.scenario.code}: ${uiState.scenario.description}",
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Coroutines — Xong: ${liveMetrics.completedRequests} | Đã hủy (Cancelled): ${liveMetrics.cancelledRequests} | Lỗi: ${liveMetrics.failedRequests}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (liveMetrics.recentLogs.isNotEmpty()) {
                    Text(
                        text = "Log: ${liveMetrics.recentLogs.first()}",
                        color = Color(0xFF4ADE80),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 5. Các nút tác vụ thực nghiệm (Auto-Scroll, Clear RAM, Clear Disk, Bảng Báo Cáo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ActionPillButton(
                text = if (uiState.isAutoScrolling) "⏹ Dừng cuộn" else "▶ Auto-Scroll (TC03)",
                containerColor = if (uiState.isAutoScrolling) Color(0xFFDC2626) else Color(0xFF0D9488),
                onClick = { onStartAutoScroll(!uiState.isAutoScrolling) },
                modifier = Modifier.weight(1.2f)
            )
            ActionPillButton(
                text = "Xóa RAM",
                containerColor = Color(0xFF475569),
                onClick = onClearRamCache,
                modifier = Modifier.weight(0.8f)
            )
            ActionPillButton(
                text = "Xóa All Cache",
                containerColor = Color(0xFF475569),
                onClick = onClearAllCaches,
                modifier = Modifier.weight(1f)
            )
            ActionPillButton(
                text = "📊 Bảng TC01-08",
                containerColor = Color(0xFF2563EB),
                onClick = onShowReportDialog,
                modifier = Modifier.weight(1.1f)
            )
        }
    }
}

@Composable
private fun TelemetryStatBox(
    title: String,
    primaryValue: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = primaryValue,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        Text(
            text = subValue,
            color = Color(0xFFCBD5E1),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionPillButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Thành phần hiển thị 1 sản phẩm trên danh sách cuộn (Yêu cầu 1, 2, 4):
 * - So sánh trực tiếp cấu hình `ImageRequest` giữa [OptimizationMode.BASELINE] và [OptimizationMode.OPTIMIZED].
 */
@Composable
private fun ProductListItemCard(
    product: ProductItem,
    mode: OptimizationMode,
    scenario: TestScenario,
    cacheBusterToken: Int,
    retryCounter: Int,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
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

    val currencyFormat = remember { NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")) }
    val rawBytesKb = remember(mode, scenario) {
        product.calculateRawBitmapBytes(mode, scenario) / 1024L
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Khung ảnh đại diện sản phẩm (92x92 dp) kèm Placeholder & Error Fallback (Yêu cầu 1 & 2)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                        // Placeholder hiển thị trong lúc đang tải bất đồng bộ
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Đang tải...",
                                fontSize = 9.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    },
                    error = {
                        // Ảnh thay thế (Fallback) khi xảy ra lỗi mạng hoặc URL hỏng (TC05, TC06)
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
                        SubcomposeAsyncImageContent()
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin sản phẩm + Nhãn kỹ thuật giải mã ảnh
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${product.code} • ${product.category}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (mode == OptimizationMode.OPTIMIZED) Color(0xFFDCFCE7)
                                else Color(0xFFFFEDD5)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (mode == OptimizationMode.OPTIMIZED) {
                                "Thumb 240px (~${rawBytesKb}KB)"
                            } else {
                                "Full 2400px (~${rawBytesKb / 1024}MB)"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == OptimizationMode.OPTIMIZED) OptimizedGreen else BaselineOrange
                        )
                    }
                }

                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${currencyFormat.format(product.priceVnd)} ₫",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD84315)
                    )
                    Text(
                        text = "★ ${String.format(Locale.US, "%.1f", product.rating)} (${product.reviewCount})",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = if (scenario == TestScenario.TC06_ERROR_AND_SLOW && product.isSimulatedErrorItem) {
                        "Kịch bản TC06: Mục này được gắn URL lỗi để kiểm thử Error Fallback"
                    } else if (scenario == TestScenario.TC06_ERROR_AND_SLOW && product.isSimulatedSlowItem) {
                        "Kịch bản TC06: Mục này giả lập máy chủ chậm (Delay 2.200ms)"
                    } else {
                        "Chạm để mở ảnh chi tiết độ phân giải cao & chẩn đoán Bitmap"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
