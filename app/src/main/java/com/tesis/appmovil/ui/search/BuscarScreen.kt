package com.tesis.appmovil.ui.search

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit

/**
 * Host para mostrar BuscarFragment dentro de Compose.
 * Requiere que exista la clase BuscarFragment en el MISMO paquete:
 * package com.tesis.appmovil.ui.search
 */
@Composable
fun BuscarFragmentHost(modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findFragmentActivity() ?: return
    val containerId = remember { View.generateViewId() }
    val fragmentTag = "BuscarFragmentTag"

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FrameLayout(ctx).apply {
                id = containerId
                val fm = activity.supportFragmentManager
                val existing = fm.findFragmentByTag(fragmentTag)
                fm.commit {
                    setReorderingAllowed(true)
                    if (existing == null) {
                        add(id, BuscarFragment(), fragmentTag)
                    }
                }
            }
        }
    )
}

/** Helper para obtener la Activity desde el Context de Compose */
private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
