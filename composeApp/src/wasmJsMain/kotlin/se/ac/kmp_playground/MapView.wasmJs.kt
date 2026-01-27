@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("OPT_IN_USAGE")

package se.ac.kmp_playground

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@JsFun("(id) => document.getElementById(id)")
private external fun getElementById(id: String): JsAny?

@JsFun("() => { const div = document.createElement('div'); div.id = 'leaflet-map'; div.style.width = '100%'; div.style.height = '100%'; div.style.position = 'absolute'; div.style.top = '0'; div.style.left = '0'; return div; }")
private external fun createMapDiv(): JsAny

@JsFun("(parent, child) => parent.appendChild(child)")
private external fun appendChild(parent: JsAny, child: JsAny)

@JsFun("(lat, lon, zoom) => { if (window.leafletMap) { window.leafletMap.remove(); } window.leafletMap = L.map('leaflet-map').setView([lat, lon], zoom); L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap contributors' }).addTo(window.leafletMap); window.leafletMarkers = []; return window.leafletMap; }")
private external fun initLeafletMap(lat: Double, lon: Double, zoom: Int): JsAny

@JsFun("(lat, lon, title, isSelected, markerId) => { const color = isSelected ? 'green' : 'blue'; const marker = L.marker([lat, lon]).addTo(window.leafletMap); marker.bindPopup(title); marker.markerId = markerId; if (isSelected) { marker.setIcon(L.icon({ iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png', shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png', iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41] })); } window.leafletMarkers.push(marker); return marker; }")
private external fun addMarker(lat: Double, lon: Double, title: String, isSelected: Boolean, markerId: Int): JsAny

@JsFun("(marker, callback) => { marker.on('click', function() { callback(marker.markerId); }); }")
private external fun setMarkerClickListener(marker: JsAny, callback: (Int) -> Unit)

@JsFun("() => { if (window.leafletMarkers) { window.leafletMarkers.forEach(m => m.remove()); window.leafletMarkers = []; } }")
private external fun clearMarkers()

@JsFun("(lat, lon) => { if (window.leafletMap) { window.leafletMap.setView([lat, lon]); } }")
private external fun setMapCenter(lat: Double, lon: Double)

@JsFun("() => { const el = document.getElementById('leaflet-map'); if (el) el.remove(); if (window.leafletMap) { window.leafletMap.remove(); window.leafletMap = null; } }")
private external fun cleanupMap()

@JsFun("(div) => { document.body.appendChild(div); }")
private external fun appendToBody(div: JsAny)

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
        val mapDiv = createMapDiv()
        appendToBody(mapDiv)

        initLeafletMap(centerLat, centerLon, zoom.toInt())
        mapInitialized = true

        onDispose {
            cleanupMap()
        }
    }

    LaunchedEffect(markers, selectedMarkerIds, mapInitialized) {
        if (mapInitialized) {
            clearMarkers()
            markers.forEach { marker ->
                val isSelected = marker.id in selectedMarkerIds
                val leafletMarker = addMarker(
                    marker.lat,
                    marker.lon,
                    marker.title,
                    isSelected,
                    marker.id
                )
                setMarkerClickListener(leafletMarker) { id ->
                    onMarkerClick(id)
                }
            }

            if (markers.isNotEmpty()) {
                val avgLat = markers.map { it.lat }.average()
                val avgLon = markers.map { it.lon }.average()
                setMapCenter(avgLat, avgLon)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize())
}