package com.tesis.appmovil.ui.search

import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.tesis.appmovil.BuscarFragment
import com.tesis.appmovil.R

@Composable
fun BuscarFragmentHost() {
    val ctx = LocalContext.current
    val activity = ctx.findFragmentActivity() ?: return
    val tag = "buscar_fragment"
    val containerId = R.id.search_fragment_container  // ID FIJO (ids.xml)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewCtx ->
            FragmentContainerView(viewCtx).apply {
                id = containerId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                post {
                    val fm = activity.supportFragmentManager
                    val existing = fm.findFragmentByTag(tag)

                    // -> Primera vez, o si la view del fragment ya no existe, o si quedó con otro container
                    if (existing == null || existing.view == null || existing.id != containerId) {
                        fm.beginTransaction()
                            .replace(
                                containerId,
                                (existing as? Fragment) ?: BuscarFragment(),
                                tag
                            )
                            .commitNowAllowingStateLoss()
                    }
                }
            }
        }
    )

    // Limpieza opcional al salir de la pestaña (si prefieres reconstruir siempre limpio)
    // Si quieres conservar el estado del mapa, comenta todo este bloque.
    DisposableEffect(Unit) {
        onDispose {
            val fm = activity.supportFragmentManager
            val frag = fm.findFragmentByTag(tag)
            if (frag != null && !activity.isFinishing) {
                fm.beginTransaction()
                    .remove(frag)
                    .commitAllowingStateLoss()
            }
        }
    }
}

/** Sube por los ContextWrapper hasta encontrar una FragmentActivity */
private fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
