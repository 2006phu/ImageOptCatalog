package com.university.imageoptimization.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.university.imageoptimization.BuildConfig
import com.university.imageoptimization.data.model.ProductItem
import com.university.imageoptimization.data.repository.ProductRepository
import com.university.imageoptimization.image.CacheStorageStats
import com.university.imageoptimization.image.ImageLoaderProvider
import com.university.imageoptimization.image.OptimizationMode
import com.university.imageoptimization.image.TestScenario
import com.university.imageoptimization.telemetry.LivePerformanceMetrics
import com.university.imageoptimization.telemetry.PerformanceTracker
import com.university.imageoptimization.telemetry.ScenarioBenchmarkRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CatalogUiState(
    val mode: OptimizationMode = OptimizationMode.fromString(BuildConfig.DEFAULT_MODE),
    val scenario: TestScenario = TestScenario.TC01_FIRST_LOAD,
    val itemCount: Int = 100,
    val products: List<ProductItem> = emptyList(),
    val selectedProduct: ProductItem? = null,
    val isOfflineSimulated: Boolean = false,
    val isAutoScrolling: Boolean = false,
    val isDashboardExpanded: Boolean = true,
    val showBenchmarkDialog: Boolean = false,
    val cacheBusterToken: Int = 1,
    val retryCounter: Int = 0,
    val cacheStats: CacheStorageStats = CacheStorageStats(0, 0, 0, 0)
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    val performanceTracker = PerformanceTracker()
    val imageLoaderProvider = ImageLoaderProvider(application, performanceTracker)

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    val liveMetrics: StateFlow<LivePerformanceMetrics> = performanceTracker.metrics

    init {
        val initialMode = OptimizationMode.fromString(BuildConfig.DEFAULT_MODE)
        val initialProducts = ProductRepository.generateProducts(100)
        _uiState.update {
            it.copy(
                mode = initialMode,
                products = initialProducts,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.updateContext(
            mode = initialMode,
            scenario = TestScenario.TC01_FIRST_LOAD,
            itemCount = 100
        )
        performanceTracker.startFrameMonitoring()
    }

    fun selectOptimizationMode(mode: OptimizationMode) {
        if (_uiState.value.mode == mode) return
        _uiState.update {
            it.copy(
                mode = mode,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.resetMetrics(
            mode = mode,
            scenario = _uiState.value.scenario,
            itemCount = _uiState.value.itemCount,
            reason = "Chuyển sang chế độ ${mode.label}"
        )
    }

    fun selectItemCount(count: Int) {
        val newProducts = ProductRepository.generateProducts(count)
        _uiState.update {
            it.copy(
                itemCount = count,
                products = newProducts,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.resetMetrics(
            mode = _uiState.value.mode,
            scenario = _uiState.value.scenario,
            itemCount = count,
            reason = "Đổi quy mô danh sách sang $count sản phẩm"
        )
    }

    fun selectScenario(scenario: TestScenario) {
        val currentState = _uiState.value
        var targetCount = currentState.itemCount
        var newOffline = currentState.isOfflineSimulated
        var newCacheBuster = currentState.cacheBusterToken

        when (scenario) {
            TestScenario.TC01_FIRST_LOAD -> {
                // TC01: Xóa sạch cache và đổi token để đo Cold Load thực sự từ mạng
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
                imageLoaderProvider.clearAllCaches()
                newCacheBuster += 1
            }
            TestScenario.TC02_WARM_CACHE -> {
                // TC02: Giữ nguyên cacheBusterToken để đọc lại từ L1 Memory / L2 Disk Cache
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
            }
            TestScenario.TC03_FAST_SCROLL -> {
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
            }
            TestScenario.TC04_HIGH_RES -> {
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
            }
            TestScenario.TC05_OFFLINE_MODE -> {
                // TC05: Bật chế độ ngắt mạng giả lập, xóa RAM cache để kiểm chứng khả năng đọc từ L2 Disk Cache
                newOffline = true
                imageLoaderProvider.isSimulatedOffline = true
                imageLoaderProvider.clearMemoryCache()
            }
            TestScenario.TC06_ERROR_AND_SLOW -> {
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
                newCacheBuster += 1
            }
            TestScenario.TC07_SCALABILITY -> {
                newOffline = false
                imageLoaderProvider.isSimulatedOffline = false
                targetCount = when (currentState.itemCount) {
                    100 -> 500
                    500 -> 1000
                    else -> 1000
                }
            }
            TestScenario.TC08_COMPARISON -> {
                _uiState.update { it.copy(scenario = scenario, showBenchmarkDialog = true) }
                return
            }
        }

        val updatedProducts = if (targetCount != currentState.itemCount) {
            ProductRepository.generateProducts(targetCount)
        } else {
            currentState.products
        }

        _uiState.update {
            it.copy(
                scenario = scenario,
                itemCount = targetCount,
                products = updatedProducts,
                isOfflineSimulated = newOffline,
                cacheBusterToken = newCacheBuster,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }

        performanceTracker.resetMetrics(
            mode = currentState.mode,
            scenario = scenario,
            itemCount = targetCount,
            reason = "Kích hoạt kịch bản ${scenario.code}: ${scenario.title}"
        )
    }

    fun toggleOfflineMode(enabled: Boolean) {
        imageLoaderProvider.isSimulatedOffline = enabled
        _uiState.update {
            it.copy(
                isOfflineSimulated = enabled,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.appendLog(
            if (enabled) "NETWORK: Đã NGẮT MẠNG giả lập (Offline Mode = ON)"
            else "NETWORK: Đã KẾT NỐI MẠNG trở lại (Offline Mode = OFF)"
        )
    }

    fun clearMemoryCacheOnly() {
        imageLoaderProvider.clearMemoryCache()
        _uiState.update {
            it.copy(
                retryCounter = it.retryCounter + 1,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.resetMetrics(reason = "Đã xóa L1 Memory Cache (Giữ nguyên L2 Disk Cache)")
    }

    fun clearAllCachesAndReload() {
        imageLoaderProvider.clearAllCaches()
        _uiState.update {
            it.copy(
                cacheBusterToken = it.cacheBusterToken + 1,
                retryCounter = it.retryCounter + 1,
                cacheStats = imageLoaderProvider.getCacheStats()
            )
        }
        performanceTracker.resetMetrics(reason = "Đã xóa toàn bộ L1 RAM + L2 Disk Cache & làm mới URL token")
    }

    fun refreshCacheStats() {
        _uiState.update {
            it.copy(cacheStats = imageLoaderProvider.getCacheStats())
        }
    }

    fun setAutoScrolling(active: Boolean) {
        _uiState.update { it.copy(isAutoScrolling = active) }
        if (active) {
            performanceTracker.appendLog("AUTO-SCROLL: Bắt đầu cuộn tự động tốc độ cao để đo Frame Jank & Cancellation")
        } else {
            refreshCacheStats()
            performanceTracker.appendLog("AUTO-SCROLL: Hoàn tất bài đo cuộn danh sách")
        }
    }

    fun toggleDashboardExpanded() {
        _uiState.update { it.copy(isDashboardExpanded = !it.isDashboardExpanded) }
    }

    fun setShowBenchmarkDialog(show: Boolean) {
        _uiState.update { it.copy(showBenchmarkDialog = show) }
    }

    fun openProductDetail(product: ProductItem) {
        refreshCacheStats()
        _uiState.update { it.copy(selectedProduct = product) }
        performanceTracker.appendLog("DETAIL: Mở màn hình chi tiết cho ${product.code} (${product.name})")
    }

    fun closeProductDetail() {
        refreshCacheStats()
        _uiState.update { it.copy(selectedProduct = null) }
    }

    fun retryFailedImages() {
        _uiState.update { it.copy(retryCounter = it.retryCounter + 1) }
        performanceTracker.appendLog("RETRY: Thực hiện tải lại các ảnh bị lỗi")
    }

    fun getReferenceBenchmarkRecords(): List<ScenarioBenchmarkRecord> {
        return performanceTracker.getReferenceComparisonTable()
    }

    override fun onCleared() {
        performanceTracker.stopFrameMonitoring()
        super.onCleared()
    }
}
