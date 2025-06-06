package com.dicoding.dbesto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dicoding.dbesto.ui.theme.JetRewardTheme

class
MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetRewardTheme {
                JetRewardApp()
            }
        }
    }
}