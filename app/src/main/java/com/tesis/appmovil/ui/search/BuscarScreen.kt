package com.tesis.appmovil.ui.search

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit

@Composable
fun BuscarFragmentHost(modifier: Modifier = Modifier) {
    val activity = LocalContext.current as FragmentActivity
    val fm = activity.supportFragmentManager
    val containerId = remember { View.generateViewId() }   // <- id VÁLIDO en runtime
    val tag = "buscar_fragment"

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = containerId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { container ->
            val existing = fm.findFragmentByTag(tag)
            fm.commit {
                setReorderingAllowed(true)
                if (existing == null) {
                    replace(container.id, BuscarFragment(), tag)
                } else {
                    // si ya existe, asegúrate de que esté asociado a ESTE contenedor
                    if (existing.view?.id != container.id) {
                        replace(container.id, existing, tag)
                    }
                }
            }
        }
    )
}
