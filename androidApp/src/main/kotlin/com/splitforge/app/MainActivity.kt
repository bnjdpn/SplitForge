package com.splitforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.splitforge.app.ui.SplitForgeApp
import com.splitforge.app.ui.theme.SplitForgeTheme
import com.splitforge.app.PlatformAppDependenciesFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dependencies = PlatformAppDependenciesFactory(applicationContext).create()
        setContent {
            SplitForgeTheme {
                SplitForgeApp(dependencies)
            }
        }
    }
}
