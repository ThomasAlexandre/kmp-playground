package se.ac.kmp_playground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun MapView(
    modifier: Modifier,
    markers: List<MapMarker>,
    selectedMarkerIds: Set<Int>,
    onMarkerClick: (Int) -> Unit,
    centerLat: Double,
    centerLon: Double,
    zoom: Float
) {
    val centerPosition = if (markers.isNotEmpty()) {
        LatLng(markers.map { it.lat }.average(), markers.map { it.lon }.average())
    } else {
        LatLng(centerLat, centerLon)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPosition, zoom)
    }

    LaunchedEffect(markers) {
        if (markers.isNotEmpty()) {
            val avgLat = markers.map { it.lat }.average()
            val avgLon = markers.map { it.lon }.average()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(avgLat, avgLon), zoom)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        markers.forEach { marker ->
            val isSelected = marker.id in selectedMarkerIds
            Marker(
                state = MarkerState(position = LatLng(marker.lat, marker.lon)),
                title = marker.title,
                alpha = if (isSelected) 1f else 0.7f,
                onClick = {
                    onMarkerClick(marker.id)
                    true
                }
            )
        }
    }
}