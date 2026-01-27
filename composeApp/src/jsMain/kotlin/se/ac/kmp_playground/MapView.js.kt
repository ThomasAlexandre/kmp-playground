package se.ac.kmp_playground

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

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
    var mapInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val mapDiv = document.createElement("div") as HTMLDivElement
        mapDiv.id = "leaflet-map"
        mapDiv.style.width = "100%"
        mapDiv.style.height = "100%"
        mapDiv.style.position = "absolute"
        mapDiv.style.top = "0"
        mapDiv.style.left = "0"
        document.body?.appendChild(mapDiv)

        val initialLat = if (markers.isNotEmpty()) markers.map { it.lat }.average() else centerLat
        val initialLon = if (markers.isNotEmpty()) markers.map { it.lon }.average() else centerLon

        js("""
            if (window.leafletMap) {
                window.leafletMap.remove();
            }
            window.leafletMap = L.map('leaflet-map').setView([initialLat, initialLon], zoom);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(window.leafletMap);
            window.leafletMarkers = [];
        """)

        mapInitialized = true

        onDispose {
            js("""
                var el = document.getElementById('leaflet-map');
                if (el) el.remove();
                if (window.leafletMap) {
                    window.leafletMap.remove();
                    window.leafletMap = null;
                }
            """)
        }
    }

    LaunchedEffect(markers, selectedMarkerIds, mapInitialized) {
        if (mapInitialized) {
            js("if (window.leafletMarkers) { window.leafletMarkers.forEach(function(m) { m.remove(); }); window.leafletMarkers = []; }")

            markers.forEach { marker ->
                val isSelected = marker.id in selectedMarkerIds
                val lat = marker.lat
                val lon = marker.lon
                val title = marker.title
                val markerId = marker.id

                js("""
                    var leafletMarker = L.marker([lat, lon]).addTo(window.leafletMap);
                    leafletMarker.bindPopup(title);
                    leafletMarker.markerId = markerId;
                    if (isSelected) {
                        leafletMarker.setIcon(L.icon({
                            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
                            iconSize: [25, 41],
                            iconAnchor: [12, 41],
                            popupAnchor: [1, -34],
                            shadowSize: [41, 41]
                        }));
                    }
                    window.leafletMarkers.push(leafletMarker);
                """)
            }

            if (markers.isNotEmpty()) {
                val avgLat = markers.map { it.lat }.average()
                val avgLon = markers.map { it.lon }.average()
                js("if (window.leafletMap) { window.leafletMap.setView([avgLat, avgLon]); }")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize())
}