package com.splitforge.app.util

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

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
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .mapNotNull { it as? platform.UIKit.UIWindowScene }
        .flatMap { it.windows }
        .firstOrNull { it.isKeyWindow }
    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

private fun ByteArray.toNSData(): NSData {
    val data = NSMutableData.create(length = size.toULong()) ?: return NSData()
    usePinned { pinned ->
        memcpy(data.mutableBytes, pinned.addressOf(0), size.convert())
    }
    return data
}
