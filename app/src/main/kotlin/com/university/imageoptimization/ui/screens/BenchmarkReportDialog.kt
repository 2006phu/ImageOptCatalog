package com.university.imageoptimization.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.university.imageoptimization.telemetry.LivePerformanceMetrics
import com.university.imageoptimization.telemetry.ScenarioBenchmarkRecord
import com.university.imageoptimization.ui.theme.BaselineOrange
import com.university.imageoptimization.ui.theme.OptimizedGreen
import java.util.Locale

@Composable
fun BenchmarkReportDialog(
    liveMetrics: LivePerformanceMetrics,
    records: List<ScenarioBenchmarkRecord>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Báo Cáo Thực Nghiệm TC01 – TC08",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "So sánh Phiên bản Cơ sở (Baseline) vs Đã tối ưu (Optimized)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tóm tắt phiên đo trực tiếp hiện tại
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Phiên đo hiện tại (${liveMetrics.scenario.code} - ${liveMetrics.mode.shortLabel} - ${liveMetrics.itemCount} ảnh):",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tải xong: ${liveMetrics.completedRequests} | Hủy (Cancel): ${liveMetrics.cancelledRequests} | Lỗi: ${liveMetrics.failedRequests}\n" +
                                "• Trung bình: ${liveMetrics.avgLoadTimeMs} ms | Cache Hit: ${String.format(Locale.US, "%.1f", liveMetrics.cacheHitRatioPercent)}%\n" +
                                "• RAM hiện tại/đỉnh: ${liveMetrics.totalRamUsedMb} / ${liveMetrics.peakRamUsedMb} MB | Jank: ${liveMetrics.jankPercent}% (${liveMetrics.currentFps} FPS)",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.scenarioCode }) { record ->
                        ScenarioBenchmarkCard(record = record)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đóng bảng báo cáo")
                }
            }
        }
    }
}

@Composable
private fun ScenarioBenchmarkCard(record: ScenarioBenchmarkRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${record.scenarioCode}: ${record.scenarioTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OptimizedGreen.copy(alpha = 0.14f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "-%d%% TG / -%d%% RAM",
                            record.loadTimeImprovementPercent.toInt(),
                            record.ramReductionPercent.toInt()
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OptimizedGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // So sánh 3 chỉ số cốt lõi
            MetricComparisonRow(
                label = "Thời gian tải TB",
                baselineText = "${record.baselineAvgLoadMs} ms",
                optimizedText = "${record.optimizedAvgLoadMs} ms"
            )
            MetricComparisonRow(
                label = "Bộ nhớ RAM đỉnh",
                baselineText = "${record.baselinePeakRamMb} MB",
                optimizedText = "${record.optimizedPeakRamMb} MB"
            )
            MetricComparisonRow(
                label = "Khung hình giật (Jank)",
                baselineText = "${record.baselineJankPercent}%",
                optimizedText = "${record.optimizedJankPercent}%"
            )
            MetricComparisonRow(
                label = "Tỷ lệ Cache Hit",
                baselineText = "${record.baselineCacheHitPercent}%",
                optimizedText = "${record.optimizedCacheHitPercent}%"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            Text(
                text = "Nhận xét: ${record.note}",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun MetricComparisonRow(
    label: String,
    baselineText: String,
    optimizedText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1.2f)
        )
        Row(
            modifier = Modifier.weight(1.8f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Base: $baselineText",
                fontSize = 12.sp,
                color = BaselineOrange,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Opt: $optimizedText",
                fontSize = 12.sp,
                color = OptimizedGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
