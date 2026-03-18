package cn.gdeiassistant.ui.datacenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.navigation.Routes

@Composable
fun DataCenterScreen(navController: NavHostController) {
    LazyScreen(
        title = stringResource(R.string.data_center_title),
        onBack = navController::popBackStack
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.electricity_title),
                    subtitle = stringResource(R.string.electricity_subtitle_short),
                    icon = Icons.Rounded.Bolt,
                    onClick = { navController.navigate(Routes.ELECTRICITY_FEES) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.yellow_page_title),
                    subtitle = stringResource(R.string.yellow_page_subtitle_short),
                    icon = Icons.Rounded.PhoneAndroid,
                    onClick = { navController.navigate(Routes.YELLOW_PAGE) },
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.data_center_section_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.size(12.dp))
                Text(stringResource(R.string.data_center_section_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
