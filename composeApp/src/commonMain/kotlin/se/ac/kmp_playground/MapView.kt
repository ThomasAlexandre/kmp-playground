package se.ac.kmp_playground

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.ac.kmp_playground.data.Store

object DefaultLocation {
    const val STOCKHOLM_LAT = 59.3293
    const val STOCKHOLM_LNG = 18.0686
}

data class MapMarker(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val title: String,
    val isSelected: Boolean = false
)

fun Store.toMapMarker(isSelected: Boolean = false) = MapMarker(
    id = this.id,
    lat = this.lat,
    lon = this.lon,
    title = this.name,
    isSelected = isSelected
)

@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    selectedMarkerIds: Set<Int> = emptySet(),
    onMarkerClick: (Int) -> Unit = {},
    centerLat: Double = DefaultLocation.STOCKHOLM_LAT,
    centerLon: Double = DefaultLocation.STOCKHOLM_LNG,
    zoom: Float = 12f
)