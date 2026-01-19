package se.ac.kmp_playground

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object DefaultLocation {
    const val STOCKHOLM_LAT = 59.3293
    const val STOCKHOLM_LNG = 18.0686
}

@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    useCurrentLocation: Boolean = true,
    fallbackLatitude: Double = DefaultLocation.STOCKHOLM_LAT,
    fallbackLongitude: Double = DefaultLocation.STOCKHOLM_LNG,
    zoom: Float = 12f
)