package se.ac.kmp_playground

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
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
    val latitude = if (markers.isNotEmpty()) markers.map { it.lat }.average() else centerLat
    val longitude = if (markers.isNotEmpty()) markers.map { it.lon }.average() else centerLon

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                val coordinate = CLLocationCoordinate2DMake(latitude, longitude)
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    10000.0 / zoom,
                    10000.0 / zoom
                )
                setRegion(region, animated = false)

                markers.forEach { marker ->
                    val annotation = MKPointAnnotation()
                    annotation.setCoordinate(CLLocationCoordinate2DMake(marker.lat, marker.lon))
                    annotation.setTitle(marker.title)
                    addAnnotation(annotation)
                }
            }
        },
        update = { view ->
            view.removeAnnotations(view.annotations)
            markers.forEach { marker ->
                val annotation = MKPointAnnotation()
                annotation.setCoordinate(CLLocationCoordinate2DMake(marker.lat, marker.lon))
                annotation.setTitle(marker.title)
                view.addAnnotation(annotation)
            }
        }
    )
}