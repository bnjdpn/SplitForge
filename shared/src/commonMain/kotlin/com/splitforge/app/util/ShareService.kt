package com.splitforge.app.util

interface ShareService {
    suspend fun shareImage(bytes: ByteArray, mimeType: String, fileName: String)
}
