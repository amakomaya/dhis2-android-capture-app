package org.dhis2.mobile.myplugin

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import org.dhis2.commons.plugin.PluginInterface

class PluginImpl : PluginInterface {

    @Composable
    override fun Show(context: Context) {
        val intent = MainActivity.getIntent(context)
          context.startActivity(intent)
    }

}
