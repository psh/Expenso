package dev.spikeysanju.expensetracker.view.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class OpenCsvContract : ActivityResultContract<Uri, Unit>() {

    override fun createIntent(context: Context, input: Uri): Intent {
        val title = "Open with"
        val csvPreviewIntent = Intent(Intent.ACTION_OPEN_DOCUMENT, input).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        return Intent.createChooser(csvPreviewIntent, title)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
    }
}
