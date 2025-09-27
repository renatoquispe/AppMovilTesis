package com.tesis.appmovil.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.tesis.appmovil.R

@Composable
fun ChatFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bellabot))
        val progress by animateLottieCompositionAsState(
            composition = comp,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(composition = comp, progress = { progress })
    }
}
