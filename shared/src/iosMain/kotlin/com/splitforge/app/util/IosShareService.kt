@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.splitforge.app.util

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

class IosShareService : ShareService {
    override suspend fun shareImage(bytes: ByteArray, mimeType: String, fileName: String) {
        dispatch_async(dispatch_get_main_queue()) {
            val data = bytes.toNSData()
            val image = UIImage(data = data)
            if (image == null) return@dispatch_async
            val items = listOf(image) as List<Any>
            val controller = UIActivityViewController(items, null)
            topViewController()?.presentViewController(controller, true, null)
        }
    }
}

private fun topViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

private fun ByteArray.toNSData(): NSData = memScoped {
    if (this@toNSData.isEmpty()) return@memScoped NSData()
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.convert())
}
