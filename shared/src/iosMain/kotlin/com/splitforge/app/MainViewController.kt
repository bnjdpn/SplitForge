package com.splitforge.app

import androidx.compose.ui.window.ComposeUIViewController
import com.splitforge.app.ui.SplitForgeApp
import com.splitforge.app.ui.theme.SplitForgeTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val dependencies = PlatformAppDependenciesFactory().create()
    return ComposeUIViewController {
        SplitForgeTheme {
            SplitForgeApp(dependencies)
        }
    }
}
