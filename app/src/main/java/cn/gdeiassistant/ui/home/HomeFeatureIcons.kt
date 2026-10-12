// Lucide 1.0.0 (ISC); derived from Web Home.vue iconMap. See design/LUCIDE-LICENSE.
package cn.gdeiassistant.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.ui.navigation.Routes

/** Home-only vectors. Icon's Material theme tint supplies currentColor in both themes. */
object HomeFeatureIcons {
    private val byRoute = mapOf(
        Routes.GRADE to lazy { vector("star", listOf(
            "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"
        )) },
        Routes.SCHEDULE to lazy { vector("calendar", listOf(
            "M8 2v4",
            "M16 2v4",
            "M 5 4 H 19 A 2 2 0 0 1 21 6 V 20 A 2 2 0 0 1 19 22 H 5 A 2 2 0 0 1 3 20 V 6 A 2 2 0 0 1 5 4 Z",
            "M3 10h18"
        )) },
        Routes.CET to lazy { vector("file-text", listOf(
            "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z",
            "M14 2v5a1 1 0 0 0 1 1h5",
            "M10 9H8",
            "M16 13H8",
            "M16 17H8"
        )) },
        Routes.GRADUATE_EXAM to lazy { vector("graduation-cap", listOf(
            "M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z",
            "M22 10v6",
            "M6 12.5V16a6 3 0 0 0 12 0v-3.5"
        )) },
        Routes.SPARE to lazy { vector("door-open", listOf(
            "M11 20H2",
            "M11 4.562v16.157a1 1 0 0 0 1.242.97L19 20V5.562a2 2 0 0 0-1.515-1.94l-4-1A2 2 0 0 0 11 4.561z",
            "M11 4H8a2 2 0 0 0-2 2v14",
            "M14 12h.01",
            "M22 20h-3"
        )) },
        Routes.LIBRARY to lazy { vector("book-open", listOf(
            "M12 7v14",
            "M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z"
        )) },
        Routes.CARD to lazy { vector("wallet-cards", listOf(
            "M 5 3 H 19 A 2 2 0 0 1 21 5 V 19 A 2 2 0 0 1 19 21 H 5 A 2 2 0 0 1 3 19 V 5 A 2 2 0 0 1 5 3 Z",
            "M3 9a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2",
            "M3 11h3c.8 0 1.6.3 2.1.9l1.1.9c1.6 1.6 4.1 1.6 5.7 0l1.1-.9c.5-.5 1.3-.9 2.1-.9H21"
        )) },
        Routes.DATA_CENTER to lazy { vector("database", listOf(
            "M 3 5 A 9 3 0 1 0 21 5 A 9 3 0 1 0 3 5 Z",
            "M3 5V19A9 3 0 0 0 21 19V5",
            "M3 12A9 3 0 0 0 21 12"
        )) },
        Routes.EVALUATE to lazy { vector("pen-line", listOf(
            "M13 21h8",
            "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"
        )) },
        Routes.MARKETPLACE to lazy { vector("shopping-cart", listOf(
            "M 7 21 A 1 1 0 1 0 9 21 A 1 1 0 1 0 7 21 Z",
            "M 18 21 A 1 1 0 1 0 20 21 A 1 1 0 1 0 18 21 Z",
            "M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"
        )) },
        Routes.DELIVERY to lazy { vector("truck", listOf(
            "M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2",
            "M15 18H9",
            "M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14",
            "M 15 18 A 2 2 0 1 0 19 18 A 2 2 0 1 0 15 18 Z",
            "M 5 18 A 2 2 0 1 0 9 18 A 2 2 0 1 0 5 18 Z"
        )) },
        Routes.LOST_FOUND to lazy { vector("search", listOf(
            "m21 21-4.34-4.34",
            "M 3 11 A 8 8 0 1 0 19 11 A 8 8 0 1 0 3 11 Z"
        )) },
        Routes.SECRET to lazy { vector("eye", listOf(
            "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",
            "M 9 12 A 3 3 0 1 0 15 12 A 3 3 0 1 0 9 12 Z"
        )) },
        Routes.DATING to lazy { vector("users", listOf(
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            "M16 3.128a4 4 0 0 1 0 7.744",
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M 5 7 A 4 4 0 1 0 13 7 A 4 4 0 1 0 5 7 Z"
        )) },
        Routes.EXPRESS to lazy { vector("heart", listOf(
            "M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5"
        )) },
        Routes.TOPIC to lazy { vector("message-circle", listOf(
            "M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092 10 10 0 1 0-4.777-4.719"
        )) },
        Routes.PHOTOGRAPH to lazy { vector("camera", listOf(
            "M13.997 4a2 2 0 0 1 1.76 1.05l.486.9A2 2 0 0 0 18.003 7H20a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.997a2 2 0 0 0 1.759-1.048l.489-.904A2 2 0 0 1 10.004 4z",
            "M 9 13 A 3 3 0 1 0 15 13 A 3 3 0 1 0 9 13 Z"
        )) },
    )

    fun forRoute(route: String): ImageVector? = byRoute[route]?.value

    private fun vector(name: String, paths: List<String>): ImageVector {
        val builder = ImageVector.Builder(
            name = "lucide-$name", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        )
        paths.forEach { data ->
            builder.addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            )
        }
        return builder.build()
    }
}
