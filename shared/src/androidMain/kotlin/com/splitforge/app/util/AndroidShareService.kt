package com.splitforge.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class AndroidShareService(private val context: Context) : ShareService {
    override suspend fun shareImage(bytes: ByteArray, mimeType: String, fileName: String) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
