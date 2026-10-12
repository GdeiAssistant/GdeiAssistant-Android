package cn.gdeiassistant.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.ui.navigation.AppFeatureCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFeatureIconsTest {
    @Test fun everyExistingHomeEntryHasAnUnfilledLinearVector() {
        AppFeatureCatalog.features.forEach { feature ->
            val vector = HomeFeatureIcons.forRoute(feature.route)
            assertNotNull(feature.route, vector)
            vector!!
            assertEquals(24.dp, vector.defaultWidth)
            assertEquals(24.dp, vector.defaultHeight)
            assertEquals(24f, vector.viewportWidth, 0f)
            assertEquals(24f, vector.viewportHeight, 0f)
            assertEquals(Color.Unspecified, vector.tintColor)
            assertTrue(vector.root.size > 0)
            vector.root.forEach { node ->
                assertTrue(node is VectorPath)
                val path = node as VectorPath
                assertNull(path.fill)
                assertNotNull(path.stroke)
                assertEquals(1.8f, path.strokeLineWidth, 0f)
                assertEquals(StrokeCap.Round, path.strokeLineCap)
                assertEquals(StrokeJoin.Round, path.strokeLineJoin)
                assertTrue(path.pathData.isNotEmpty())
            }
        }
    }

    @Test fun cachedVectorsAndUnknownRouteFallbackPreserveHomeBehavior() {
        AppFeatureCatalog.features.forEach { feature ->
            assertSame(HomeFeatureIcons.forRoute(feature.route), HomeFeatureIcons.forRoute(feature.route))
        }
        assertNull(HomeFeatureIcons.forRoute("no-such-home-route"))
    }
}
