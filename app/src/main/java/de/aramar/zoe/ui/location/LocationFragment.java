package de.aramar.zoe.ui.location;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import de.aramar.zoe.R;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.vehicles.Asset;
import de.aramar.zoe.data.kamereon.vehicles.Rendition;
import de.aramar.zoe.data.kamereon.vehicles.VehicleDetails;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.BackendTraffic;
import de.aramar.zoe.ui.home.HomeViewModel;
import lombok.SneakyThrows;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ImageLoader imageLoader;

    private HomeViewModel homeViewModel;

    private Marker vehicleMarker;

    private BitmapDescriptor vehicleMarkerBitmapDescriptor;

    private Location location;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);

        this.imageLoader = BackendTraffic
                .getInstance(this.getContext())
                .getImageLoader();

        this.homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        this.homeViewModel
                .getVehicles()
                .observe(this.getViewLifecycleOwner(), new Observer<Vehicles>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onChanged(@Nullable Vehicles vehicles) {
                        LocationFragment.this.homeViewModel.updateLocation(vehicles
                                .getVehicleLinks()
                                .get(0)
                                .getVin());
                        LocationFragment.this.loadMarkerImage(vehicles
                                .getVehicleLinks()
                                .get(0)
                                .getVehicleDetails());
                    }
                });
        this.homeViewModel
                .getLocation()
                .observe(this.getViewLifecycleOwner(), new Observer<Location>() {
                    @Override
                    public void onChanged(@Nullable Location location) {
                        LocationFragment.this.location = location;
                        if (LocationFragment.this.mMap != null) {
                            LocationFragment.this.setMarker(location);
                        }
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_location, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this
                .getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the
     * . This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        if (this.location != null) {
            this.setMarker(this.location);
        } else {
            // Add a marker in Waging
            LatLng wagingPosition = new LatLng(47.944108, 12.7529453);
            this.vehicleMarker = this.mMap.addMarker(new MarkerOptions()
                    .position(wagingPosition)
                    .title("ZOE"));
            this.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(wagingPosition, 12.0f)); // TODO: settings
        }
    }

    /**
     * Set the maps marker based on the vehicle position.
     *
     * @param location Location object received from Kamereon framework
     */
    private void setMarker(Location location) {
        if (location != null && location.getData() != null && location
                .getData()
                .getAttributes() != null) {
            // Add a marker in Sydney and move the camera
            LatLng zoePosition = new LatLng(location
                    .getData()
                    .getAttributes()
                    .getGpsLatitude(), location
                    .getData()
                    .getAttributes()
                    .getGpsLongitude());
            this.vehicleMarker = this.mMap.addMarker(new MarkerOptions()
                    .position(zoePosition)
                    .snippet(this.getLocalizedTimestamp(location
                            .getData()
                            .getAttributes()
                            .getLastUpdateTime()))
                    .title(location
                            .getData()
                            .getId()));
            if (this.vehicleMarkerBitmapDescriptor != null) {
                this.vehicleMarker.setIcon(this.vehicleMarkerBitmapDescriptor);
            }
            this.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(zoePosition, 15.0f)); // TODO: settings
        }
    }

    @SneakyThrows
    private String getLocalizedTimestamp(String stringTimestamp) {
        String formattedTimestamp = stringTimestamp;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = Instant.parse(stringTimestamp);
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());
            formattedTimestamp = formatter.format(instant);
        }

        return formattedTimestamp;
    }

    private void loadMarkerImage(final VehicleDetails vehicle) {
        // Try to get the image of the vehicle
        List<Asset> assets = vehicle.getAssets();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            if (asset
                    .getAssetType()
                    .compareTo("PICTURE") == 0) {
                List<Rendition> renditions = asset.getRenditions();
                for (int j = 0; j < renditions.size(); j++) {
                    Rendition rendition = renditions.get(j);
                    if (rendition
                            .getResolutionType()
                            .compareTo("ONE_MYRENAULT_SMALL") == 0) {
                        this.imageLoader.get(rendition.getUrl(), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response,
                                                   boolean isImmediate) {
                                if (response.getBitmap() != null) {
                                    LocationFragment.this.vehicleMarkerBitmapDescriptor =
                                            BitmapDescriptorFactory.fromBitmap(
                                                    response.getBitmap());
                                    if (LocationFragment.this.vehicleMarker != null) {
                                        LocationFragment.this.vehicleMarker.setIcon(
                                                LocationFragment.this.vehicleMarkerBitmapDescriptor);
                                    }
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                    }
                }
            }
        }
    }
}