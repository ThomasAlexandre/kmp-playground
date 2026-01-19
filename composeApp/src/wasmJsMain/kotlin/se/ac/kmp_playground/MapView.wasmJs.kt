package se.ac.kmp_playground

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun MapView(
    modifier: Modifier,
    useCurrentLocation: Boolean,
    fallbackLatitude: Double,
    fallbackLongitude: Double,
    zoom: Float
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Map: Stockholm ($fallbackLatitude, $fallbackLongitude)",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}