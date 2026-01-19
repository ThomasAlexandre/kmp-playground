package se.ac.kmp_playground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    modifier: Modifier,
    useCurrentLocation: Boolean,
    fallbackLatitude: Double,
    fallbackLongitude: Double,
    zoom: Float
) {
    var latitude by remember { mutableStateOf(fallbackLatitude) }
    var longitude by remember { mutableStateOf(fallbackLongitude) }
    var mapView by remember { mutableStateOf<MKMapView?>(null) }

    val locationManager = remember { CLLocationManager() }

    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                val location = didUpdateLocations.lastOrNull() as? platform.CoreLocation.CLLocation
                location?.coordinate?.useContents {
                    latitude = this.latitude
                    longitude = this.longitude
                    updateMapRegion(mapView, this.latitude, this.longitude, zoom)
                }
                manager.stopUpdatingLocation()
            }

            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                val status = manager.authorizationStatus
                if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
                ) {
                    manager.startUpdatingLocation()
                }
            }
        }
    }

    DisposableEffect(useCurrentLocation) {
        if (useCurrentLocation) {
            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest

            val status = locationManager.authorizationStatus
            when (status) {
                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways -> {
                    locationManager.startUpdatingLocation()
                }
                else -> {
                    locationManager.requestWhenInUseAuthorization()
                }
            }
        }

        onDispose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                mapView = this
                val coordinate = CLLocationCoordinate2DMake(latitude, longitude)
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    10000.0 / zoom,
                    10000.0 / zoom
                )
                setRegion(region, animated = false)

                val annotation = MKPointAnnotation()
                annotation.setCoordinate(coordinate)
                annotation.setTitle("Location")
                addAnnotation(annotation)

                showsUserLocation = useCurrentLocation
            }
        },
        update = { view ->
            mapView = view
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun updateMapRegion(mapView: MKMapView?, lat: Double, lng: Double, zoom: Float) {
    mapView?.let { view ->
        val coordinate = CLLocationCoordinate2DMake(lat, lng)
        val region = MKCoordinateRegionMakeWithDistance(
            coordinate,
            10000.0 / zoom,
            10000.0 / zoom
        )
        view.setRegion(region, animated = true)

        view.removeAnnotations(view.annotations)
        val annotation = MKPointAnnotation()
        annotation.setCoordinate(coordinate)
        annotation.setTitle("Current Location")
        view.addAnnotation(annotation)
    }
}