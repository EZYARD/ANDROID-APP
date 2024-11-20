import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sprint0backend.BackendWrapper
import com.example.sprint0backend.ListingComponent
import com.google.android.gms.maps.model.LatLng
import java.lang.reflect.Modifier

@Composable
fun MapScreen() {
    val userZipCode = "12345"
    val radiusMiles = 20.0
    var listings by rememberSaveable { mutableStateOf<List<ListingComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    // Convert zip code to LatLng (you might need to use Geocoding API)
    val userLocation = LatLng(34.45448,-119.80085) // Example coordinates

    LaunchedEffect(Unit) {
        BackendWrapper.getListings(
            onSuccess = { backendListings ->
                listings = backendListings
                isLoading = false
            },
            onError = { error ->
                isLoading = false
            }
        )
    }


    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                top = 64.dp,
                bottom = 128.dp,
                start = 16.dp, // Left-Right
                end = 16.dp)
    ) {
        YardSaleMap(
            userLocation = userLocation,
            yardSales = listings,
            radiusMiles = radiusMiles
        )
    }
}
