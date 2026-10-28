package com.university.imageoptimization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.university.imageoptimization.ui.screens.BenchmarkReportDialog
import com.university.imageoptimization.ui.screens.ProductCatalogScreen
import com.university.imageoptimization.ui.screens.ProductDetailScreen
import com.university.imageoptimization.ui.theme.ImageOptCatalogTheme
import com.university.imageoptimization.ui.viewmodel.CatalogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageOptCatalogTheme {
                val viewModel: CatalogViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val liveMetrics by viewModel.liveMetrics.collectAsState()

                val activeImageLoader = viewModel.imageLoaderProvider.getLoader(uiState.mode)

                val selectedProduct = uiState.selectedProduct
                if (selectedProduct != null) {
                    BackHandler {
                        viewModel.closeProductDetail()
                    }
                    ProductDetailScreen(
                        product = selectedProduct,
                        mode = uiState.mode,
                        cacheBusterToken = uiState.cacheBusterToken,
                        imageLoader = activeImageLoader,
                        onBack = { viewModel.closeProductDetail() }
                    )
                } else {
                    ProductCatalogScreen(
                        uiState = uiState,
                        liveMetrics = liveMetrics,
                        imageLoader = activeImageLoader,
                        onSelectMode = viewModel::selectOptimizationMode,
                        onSelectItemCount = viewModel::selectItemCount,
                        onSelectScenario = viewModel::selectScenario,
                        onToggleOffline = viewModel::toggleOfflineMode,
                        onClearRamCache = viewModel::clearMemoryCacheOnly,
                        onClearAllCaches = viewModel::clearAllCachesAndReload,
                        onStartAutoScroll = viewModel::setAutoScrolling,
                        onToggleDashboard = viewModel::toggleDashboardExpanded,
                        onShowReportDialog = { viewModel.setShowBenchmarkDialog(true) },
                        onRetryFailed = viewModel::retryFailedImages,
                        onProductClick = viewModel::openProductDetail
                    )
                }

                if (uiState.showBenchmarkDialog) {
                    BenchmarkReportDialog(
                        liveMetrics = liveMetrics,
                        records = viewModel.getReferenceBenchmarkRecords(),
                        onDismiss = { viewModel.setShowBenchmarkDialog(false) }
                    )
                }
            }
        }
    }
}
