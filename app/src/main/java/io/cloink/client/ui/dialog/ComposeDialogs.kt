package io.cloink.client.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

object ComposeDialogs {
    @JvmStatic
    fun showAlwaysOn(activity: Activity, onDismiss: Runnable?) {
        show(activity, onDismiss) { dismiss ->
            AlwaysOnDialogContent(activity.getString(R.string.dialog_always_on_desc), dismiss)
        }
    }

    @JvmStatic
    fun showUpdatePrompt(activity: Activity, version: String, onDownload: Runnable) {
        show(activity) { dismiss ->
            UpdateDialogContent(
                message = activity.getString(R.string.update_available_message, version),
                onLater = dismiss,
                onDownload = {
                    dismiss()
                    onDownload.run()
                },
            )
        }
    }

    private fun show(
        activity: Activity,
        onDismiss: Runnable? = null,
        content: @Composable ((() -> Unit) -> Unit),
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = ComponentDialog(activity)
        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent { CloinkTheme { content { dialog.dismiss() } } }
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(composeView)
        dialog.setOnDismissListener { onDismiss?.run() }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            decorView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}

@Composable
internal fun AlwaysOnDialogContent(descriptionHtml: String, onClose: () -> Unit) {
    DialogSurface {
        Text(
            stringResource(R.string.always_on_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            AnnotatedString.fromHtml(descriptionHtml),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_close))
        }
    }
}

@Composable
internal fun UpdateDialogContent(
    message: String,
    onLater: () -> Unit,
    onDownload: () -> Unit,
) {
    DialogSurface {
        Text(stringResource(R.string.update_available_title), style = MaterialTheme.typography.headlineSmall)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.update_later))
        }
        Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.update_download))
        }
    }
}

@Composable
private fun DialogSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }
    }
}
