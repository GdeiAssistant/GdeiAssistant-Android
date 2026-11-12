package cn.gdeiassistant.ui.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cn.gdeiassistant.R

/** One mapping from an interaction module id to its display name, shared by both lists. */
@Composable
fun interactionModuleLabel(module: String?): String = when (module) {
    "secret" -> stringResource(R.string.messages_module_secret)
    "secondhand" -> stringResource(R.string.messages_module_marketplace)
    "lostandfound" -> stringResource(R.string.messages_module_lost_found)
    "topic" -> stringResource(R.string.messages_module_topic)
    "express" -> stringResource(R.string.messages_module_express)
    "dating" -> stringResource(R.string.messages_module_dating)
    "delivery" -> stringResource(R.string.messages_module_delivery)
    "photograph" -> stringResource(R.string.messages_module_photograph)
    else -> stringResource(R.string.messages_interaction_badge)
}
