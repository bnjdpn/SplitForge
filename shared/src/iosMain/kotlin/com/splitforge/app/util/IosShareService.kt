@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.splitforge.app.util

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytesNoCopy
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

class IosShareService : ShareService {
    override suspend fun shareImage(bytes: ByteArray, mimeType: String, fileName: String) {
        dispatch_async(dispatch_get_main_queue()) {
            if (bytes.isEmpty()) return@dispatch_async
            bytes.usePinned { pinned ->
                val data = NSData.dataWithBytesNoCopy(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.convert(),
                    freeWhenDone = false
                ) ?: return@usePinned
                val image = UIImage(data = data) ?: return@usePinned
                val items = listOf(image) as List<Any>
                val controller = UIActivityViewController(items, null)
                topViewController()?.presentViewController(controller, true, null)
            }
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

