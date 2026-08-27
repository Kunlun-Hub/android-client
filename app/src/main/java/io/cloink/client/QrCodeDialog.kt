package io.cloink.client

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.cloink.client.ui.theme.CloinkTheme

class QrCodeDialog : DialogFragment() {
    private var loginSucceeded = false

    override fun onCreateDialog(state: Bundle?): Dialog {
        val url = requireArguments().getString(ARG_URL).orEmpty()
        val userCode = requireArguments().getString(ARG_USER_CODE).orEmpty()
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setContentView(ComposeView(requireContext()).apply {
                setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent { CloinkTheme { QrCodeDialogContent(url, userCode) { dismiss() } } }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            decorView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult(
            RESULT_KEY,
            Bundle().apply { putBoolean(RESULT_CANCELLED, !loginSucceeded) },
        )
    }

    fun dismissForLoginSuccess() {
        loginSucceeded = true
        dismiss()
    }

    companion object {
        const val TAG = "QrCodeDialog"
        const val RESULT_KEY = "qrCodeDialogResult"
        const val RESULT_CANCELLED = "cancelled"
        private const val ARG_URL = "url"
        private const val ARG_USER_CODE = "userCode"

        @JvmStatic
        fun newInstance(url: String, userCode: String?): QrCodeDialog =
            QrCodeDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_USER_CODE, userCode)
                }
            }
    }
}

@Composable
internal fun QrCodeDialogContent(url: String, userCode: String, onClose: () -> Unit) {
    val bitmap = remember(url) { generateQrCode(url) }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(stringResource(R.string.device_login_title), style = MaterialTheme.typography.headlineSmall)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = stringResource(R.string.qr_code_description),
                        modifier = Modifier.sizeIn(maxWidth = 320.dp, maxHeight = 320.dp),
                    )
                }
                if (userCode.isNotBlank()) {
                    Text(
                        stringResource(R.string.device_code, userCode),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        }
    }
}

private fun generateQrCode(url: String): Bitmap? = runCatching {
    BarcodeEncoder().encodeBitmap(url, BarcodeFormat.QR_CODE, 640, 640)
}.getOrNull()
