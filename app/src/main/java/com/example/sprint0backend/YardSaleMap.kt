import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sprint0backend.ListingComponent
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun YardSaleMap(
    userLocation: LatLng,
    yardSales: List<ListingComponent>,
    radiusMiles: Double
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 9f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Draw Circle
            Circle(
                center = userLocation,
                radius = radiusMiles * 1609.34, // Convert miles to meters
                fillColor = Color(0x220000FF), // Semi-transparent fill
                strokeColor = Color.Blue,
                strokeWidth = 2f
            )

            // Add Yard Sale Markers with InfoWindows displaying titles
            yardSales.forEach { sale ->
                val markerState = MarkerState(position = LatLng(sale.latitude!!, sale.longitude!!))
                markerState.showInfoWindow()
                val mark = Marker(
                    state = markerState,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    title = sale.name, // Set the title directly on the marker
                )
            }
        }
    }
}
