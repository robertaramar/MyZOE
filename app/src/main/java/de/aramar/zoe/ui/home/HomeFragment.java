package de.aramar.zoe.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import de.aramar.zoe.R;
import de.aramar.zoe.data.Summary;
import de.aramar.zoe.data.kamereon.battery.Attributes;
import de.aramar.zoe.data.kamereon.vehicles.Asset;
import de.aramar.zoe.data.kamereon.vehicles.Rendition;
import de.aramar.zoe.data.kamereon.vehicles.VehicleLink;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.BackendTraffic;
import lombok.SneakyThrows;

public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = HomeViewModel.class.getCanonicalName();

    private HomeViewModel homeViewModel;

    private TextView odometerValueTextView;

    private TextView batteryValueTextView;

    private Spinner finSpinner;

    private Switch batteryPlugSwitch;

    private Switch batteryChargeSwitch;

    private ImageView vehicleImage;

    private ProgressBar batteryProgressBar;

    private TextView batteryTemperatureTextView;

    private TextView batteryAutonomyTextView;

    private TextView batteryCapacityTextView;

    private TextView batteryEnergyTextView;

    private TextView batteryRemainingTimeTextView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String currentVin;

    private ImageLoader imageLoader;

    private TableRow capacityRow;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);

        this.imageLoader = BackendTraffic
                .getInstance(this.getContext())
                .getImageLoader();

        this.homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        this.homeViewModel
                .getSummary()
                .observe(this.getViewLifecycleOwner(), new Observer<Summary>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onChanged(@Nullable Summary summary) {
                        HomeFragment.this.updateData(summary);
                        if (HomeFragment.this.swipeRefreshLayout.isRefreshing()) {
                            HomeFragment.this.swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
        this.homeViewModel
                .getVehicles()
                .observe(this.getViewLifecycleOwner(), new Observer<Vehicles>() {
                    @Override
                    public void onChanged(@Nullable Vehicles vehicles) {
                        ArrayAdapter<Object> vehiclesArrayAdapter =
                                new ArrayAdapter<>(HomeFragment.this.getContext(),
                                        android.R.layout.simple_spinner_dropdown_item, vehicles
                                        .getVehicleLinks()
                                        .toArray());
                        vehiclesArrayAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        HomeFragment.this.finSpinner.setAdapter(vehiclesArrayAdapter);
                        // Disable if there is only one entry
                        HomeFragment.this.finSpinner.setEnabled(vehicles
                                .getVehicleLinks()
                                .size() > 1);
                    }
                });

        this.swipeRefreshLayout.setRefreshing(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        this.odometerValueTextView = root.findViewById(R.id.odometer_value);
        this.batteryValueTextView = root.findViewById(R.id.battery_level_value);
        this.batteryPlugSwitch = root.findViewById(R.id.battery_plug_switch);
        this.batteryPlugSwitch.setClickable(false);
        this.batteryChargeSwitch = root.findViewById(R.id.battery_charge_switch);
        this.batteryChargeSwitch.setClickable(false);
        this.vehicleImage = root.findViewById(R.id.vehicleImage);
        this.batteryProgressBar = root.findViewById(R.id.battery_progress_bar);
        this.batteryTemperatureTextView = root.findViewById(R.id.battery_temperature_value);
        this.batteryAutonomyTextView = root.findViewById(R.id.battery_autonomy_value);
        this.batteryCapacityTextView = root.findViewById(R.id.battery_capacity_value);
        this.batteryEnergyTextView = root.findViewById(R.id.battery_energy_value);
        this.batteryRemainingTimeTextView = root.findViewById(R.id.battery_remaining_time_value);

        this.swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        this.capacityRow = (TableRow) this.batteryCapacityTextView.getParent();
        this.capacityRow.setVisibility(View.GONE);

        // Init the vehicles drop-down
        this.finSpinner = root.findViewById(R.id.fin_spinner);
        this.finSpinner.setOnItemSelectedListener(this);

        return root;
    }

    @SneakyThrows
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Log.i(TAG, "Got an item change to position = " + position + " id = " + id);
        Object item = adapterView
                .getAdapter()
                .getItem(position);
        if (item instanceof VehicleLink) {
            this.changeCar((VehicleLink) item);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.i(TAG, "Got an onNothingSelected");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            this.swipeRefreshLayout.setRefreshing(true);
            this.triggerUpdate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        this.triggerUpdate();
    }

    /**
     * Change to a (new) vehicle in case multiple vehicles are available or the first has been selected.
     *
     * @param vehicle vehicle data from Kamereon framework
     */
    private void changeCar(VehicleLink vehicle) {
        Log.i(TAG, "Vehicle = " + vehicle);
        this.currentVin = vehicle.getVin();
        this.homeViewModel.updateBatteryStatus(this.currentVin);
        this.homeViewModel.updateCockpit(this.currentVin);

        // Try to get the image of the vehicle
        List<Asset> assets = vehicle
                .getVehicleDetails()
                .getAssets();
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
                            .compareTo("ONE_MYRENAULT_LARGE") == 0) {
                        this.imageLoader.get(rendition.getUrl(), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response,
                                                   boolean isImmediate) {
                                HomeFragment.this.vehicleImage.setImageBitmap(response.getBitmap());
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

    /**
     * Transfer received vehicle data into the UI elements.
     *
     * @param summary Received vehicle data (cockpit and battery)
     */
    @SuppressLint("DefaultLocale")
    private void updateData(Summary summary) {
        if (summary.getBattery() != null && summary
                .getBattery()
                .getData() != null && summary
                .getBattery()
                .getData()
                .getAttributes() != null) {
            Attributes attributes = summary
                    .getBattery()
                    .getData()
                    .getAttributes();
            this.batteryValueTextView.setText(String.format("%d%%", attributes.getBatteryLevel()));
            this.batteryProgressBar.setProgress(attributes.getBatteryLevel());
            this.batteryPlugSwitch.setChecked(attributes.getPlugStatus() != 0);
            this.batteryChargeSwitch.setChecked(attributes.getChargingStatus() >= 1.0);
            this.batteryTemperatureTextView.setText(
                    this.toTemperatureString(attributes.getBatteryTemperature()));
            this.batteryAutonomyTextView.setText(this.toOdometerString(attributes
                    .getBatteryAutonomy()
                    .doubleValue()));
            this.batteryCapacityTextView.setText(
                    String.format("%d kW/h", attributes.getBatteryCapacity()));
            this.capacityRow.setVisibility(
                    attributes.getBatteryCapacity() > 0 ? View.VISIBLE : View.GONE);  // useless?
            this.batteryEnergyTextView.setText(
                    String.format("%d kW/h", attributes.getBatteryAvailableEnergy()));
            this.batteryRemainingTimeTextView.setText(
                    String.format("%d m", attributes.getChargingRemainingTime()));
        }
        if (summary.getCockpit() != null) {
            this.odometerValueTextView.setText(this.toOdometerString(summary
                    .getCockpit()
                    .getData()
                    .getAttributes()
                    .getTotalMileage()));
        }
    }

    /**
     * I18N method to produce an odometer string. Will differentiate between km and mi
     *
     * @param value to be displayed
     * @return computed string
     */
    private String toOdometerString(Double value) {
        return String.format("%.2f km", value); // TODO: settings
    }

    /**
     * I18N method to produce a temperature string. Will differentiate between °C and °F
     *
     * @param value to be displayed
     * @return computed string
     */
    private String toTemperatureString(int value) {
        return String.format("%d°C", value); // TODO: settings
    }

    /**
     * Worker for triggering the update from swipe2refresh as well as from menu action.
     */
    private void triggerUpdate() {
        this.homeViewModel.updateCockpit(this.currentVin);
        this.homeViewModel.updateBatteryStatus(this.currentVin);
    }
}