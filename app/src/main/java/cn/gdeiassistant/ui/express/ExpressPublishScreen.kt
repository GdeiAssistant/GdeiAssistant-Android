package cn.gdeiassistant.ui.express

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ExpressGender
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExpressPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ExpressPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nickname by rememberSaveable { mutableStateOf("") }
    var realName by rememberSaveable { mutableStateOf("") }
    var targetName by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var selfGender by rememberSaveable { mutableStateOf(ExpressGender.SECRET.name) }
    var targetGender by rememberSaveable { mutableStateOf(ExpressGender.SECRET.name) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExpressPublishEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                ExpressPublishEvent.Submitted -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(Routes.EXPRESS_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.express_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            HeroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgePill(text = stringResource(R.string.express_publish_title), onGradient = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.express_publish_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.express_publish_nickname_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.express_publish_real_name_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetName,
                    onValueChange = { targetName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.express_publish_target_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.express_publish_content_label)) },
                    minLines = 4
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.express_publish_self_gender_label),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                GenderChips(
                    selected = selfGender,
                    onSelected = { selfGender = it.name }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.express_publish_target_gender_label),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                GenderChips(
                    selected = targetGender,
                    onSelected = { targetGender = it.name }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.submit(
                            nickname = nickname,
                            realName = realName,
                            selfGender = ExpressGender.valueOf(selfGender),
                            targetName = targetName,
                            content = content,
                            targetGender = ExpressGender.valueOf(targetGender)
                        )
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.express_publish_submit_action))
                }
            }
        }
    }
}

@Composable
private fun GenderChips(
    selected: String,
    onSelected: (ExpressGender) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ExpressGender.entries.forEach { gender ->
            FilterChip(
                selected = selected == gender.name,
                onClick = { onSelected(gender) },
                label = { Text(text = expressPublishGenderLabel(gender)) }
            )
        }
    }
}

@Composable
private fun expressPublishGenderLabel(gender: ExpressGender): String {
    return when (gender) {
        ExpressGender.MALE -> stringResource(R.string.express_gender_male)
        ExpressGender.FEMALE -> stringResource(R.string.express_gender_female)
        ExpressGender.SECRET -> stringResource(R.string.express_gender_secret)
    }
}
