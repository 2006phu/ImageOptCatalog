package com.university.imageoptimization

import com.university.imageoptimization.data.repository.ProductRepository
import com.university.imageoptimization.image.OptimizationMode
import com.university.imageoptimization.image.TestScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bộ Unit Test kiểm chứng tính đúng đắn của:
 * - Bộ sinh dữ liệu quy mô 100 / 500 / 1.000 ảnh (Yêu cầu 1 & TC07).
 * - Chiến lược phân tách URL Thumbnail (300x300) vs Full-Res (2400x1600) và URL lỗi/chậm (Yêu cầu 4 & TC06).
 * - Công thức tính dung lượng bộ nhớ Bitmap trước và sau tối ưu (Chủ đề 5 & Yêu cầu 4).
 */
class ImageOptimizationUnitTest {

    @Test
    fun testGenerateProductsSupports100_500_1000ItemsDeterministically() {
        val list100 = ProductRepository.generateProducts(100)
        val list500 = ProductRepository.generateProducts(500)
        val list1000 = ProductRepository.generateProducts(1000)

        assertEquals(100, list100.size)
        assertEquals(500, list500.size)
        assertEquals(1000, list1000.size)

        // Kiểm chứng tính tất định (Deterministic): Cùng vị trí phải có cùng seed URL
        assertEquals(list100.first().thumbnailUrl, list1000.first().thumbnailUrl)
        assertEquals(list100[99].highResImageUrl, list500[99].highResImageUrl)
    }

    @Test
    fun testResolveListImageUrlSelectsThumbnailForOptimizedAndHighResForBaseline() {
        val item = ProductRepository.generateProducts(10).first()

        val baselineUrl = item.resolveListImageUrl(
            mode = OptimizationMode.BASELINE,
            scenario = TestScenario.TC01_FIRST_LOAD,
            cacheBusterToken = 1
        )
        val optimizedUrl = item.resolveListImageUrl(
            mode = OptimizationMode.OPTIMIZED,
            scenario = TestScenario.TC01_FIRST_LOAD,
            cacheBusterToken = 1
        )

        assertTrue("Baseline phải tải ảnh gốc 2400/1600", baselineUrl.contains("/2400/1600"))
        assertTrue("Optimized phải tải ảnh thumbnail 300/300", optimizedUrl.contains("/300/300"))
    }

    @Test
    fun testTC04ForcesHighResUrlAndTC06InjectsBrokenAndSlowUrls() {
        val products = ProductRepository.generateProducts(20)
        val errorItem = products.first { it.isSimulatedErrorItem }
        val slowItem = products.first { it.isSimulatedSlowItem }

        val tc06ErrorUrl = errorItem.resolveListImageUrl(
            mode = OptimizationMode.OPTIMIZED,
            scenario = TestScenario.TC06_ERROR_AND_SLOW,
            cacheBusterToken = 2
        )
        val tc06SlowUrl = slowItem.resolveListImageUrl(
            mode = OptimizationMode.OPTIMIZED,
            scenario = TestScenario.TC06_ERROR_AND_SLOW,
            cacheBusterToken = 2
        )
        val tc04Url = slowItem.resolveListImageUrl(
            mode = OptimizationMode.OPTIMIZED,
            scenario = TestScenario.TC04_HIGH_RES,
            cacheBusterToken = 2
        )

        assertTrue(tc06ErrorUrl.contains("invalid-domain.university.edu.vn"))
        assertTrue(tc06SlowUrl.contains("sim_delay_ms=2200"))
        assertTrue(tc04Url.contains("/2400/1600"))
    }

    @Test
    fun testCalculateRawBitmapBytesDemonstratesMemorySavingsOver98Percent() {
        val item = ProductRepository.generateProducts(1).first()

        val baselineBytes = item.calculateRawBitmapBytes(
            mode = OptimizationMode.BASELINE,
            scenario = TestScenario.TC01_FIRST_LOAD
        )
        val optimizedBytes = item.calculateRawBitmapBytes(
            mode = OptimizationMode.OPTIMIZED,
            scenario = TestScenario.TC01_FIRST_LOAD
        )

        // 2400 * 1600 * 4 = 15,360,000 bytes (~15.36 MB)
        assertEquals(15_360_000L, baselineBytes)
        // 240 * 240 * 2 = 115,200 bytes (~112.5 KB)
        assertEquals(115_200L, optimizedBytes)
        assertTrue("Optimized tiết kiệm > 99% RAM trên mỗi Bitmap", optimizedBytes < baselineBytes / 100)
    }
}
