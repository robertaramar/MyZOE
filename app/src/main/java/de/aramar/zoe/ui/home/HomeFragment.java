package de.aramar.zoe.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.aramar.zoe.R;
import de.aramar.zoe.data.Summary;
import de.aramar.zoe.data.kamereon.battery.Attributes;
import de.aramar.zoe.data.kamereon.vehicles.Asset;
import de.aramar.zoe.data.kamereon.vehicles.VehicleLink;
import de.aramar.zoe.network.BackendTraffic;
import de.aramar.zoe.utilities.Tools;
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

    /**
     * Access to preferences store.
     */
    private SharedPreferences defaultSharedPreferences;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);

        this.defaultSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getContext());

        this.imageLoader = BackendTraffic
                .getInstance(this.getContext())
                .getImageLoader();

        this.homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        this.homeViewModel
                .getSummary()
                .observe(this.getViewLifecycleOwner(), summary -> {
                    HomeFragment.this.updateData(summary);
                    if (HomeFragment.this.swipeRefreshLayout.isRefreshing()) {
                        HomeFragment.this.swipeRefreshLayout.setRefreshing(false);
                    }
                });
        this.homeViewModel
                .getVehicles()
                .observe(this.getViewLifecycleOwner(), vehicles -> {
                    Object[] zoes = Objects.requireNonNull(vehicles
                            .getVehicleLinks()
                            .stream()
                            .filter(vehicleLink -> vehicleLink.getVehicleDetails()
                                    .getModel()
                                    .getLabel()
                                    .compareToIgnoreCase("ZOE") == 0)
                            .toArray());
                    ArrayAdapter<Object> vehiclesArrayAdapter =
                            new ArrayAdapter<>(HomeFragment.this.requireContext(),
                                    android.R.layout.simple_spinner_dropdown_item,
                                    zoes);
                    vehiclesArrayAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    HomeFragment.this.finSpinner.setAdapter(vehiclesArrayAdapter);
                    // Disable if there is only one entry
                    HomeFragment.this.finSpinner.setEnabled(zoes.length > 1);
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
        if (assets != null) {
            assets.stream()
                    .filter(asset -> asset.getAssetType()
                            .compareTo("PICTURE") == 0)
                    .findFirst()
                    .ifPresent(asset -> {
                        asset.getRenditions()
                                .stream()
                                .filter(rendition -> rendition.getResolutionType()
                                        .compareTo("ONE_MYRENAULT_LARGE") == 0)
                                .findFirst()
                                .ifPresent(rendition -> {
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
                                });
                    });
        }
    }

    /**
     * Transfer received vehicle data into the UI elements.
     *
     * @param summary Received vehicle data (cockpit and battery)
     */
    @SuppressLint("DefaultLocale")
    private void updateData(Summary summary) {
        String labelVoid = this
                .requireContext()
                .getResources()
                .getString(R.string.label_void);
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

            String batteryLevel = (attributes.getBatteryLevel() != null) ? String.format("%d%%",
                    attributes.getBatteryLevel()) : labelVoid;
            this.batteryValueTextView.setText(batteryLevel);
            this.batteryProgressBar.setProgress(
                    attributes.getBatteryLevel() != null ? attributes.getBatteryLevel() : 0);

            this.batteryPlugSwitch.setChecked(attributes.getPlugStatus() != 0);

            if (attributes.getChargingStatus() != null) {
                // V2 API
                this.batteryChargeSwitch.setChecked(attributes.getChargingStatus() >= 1.0);
            } else {
                // V1 API
                Object chargeStatus = null;
                if (attributes
                        .getAdditionalProperties()
                        .containsKey("chargeStatus")) {
                    chargeStatus = attributes
                            .getAdditionalProperties()
                            .get("chargeStatus");
                }
                this.batteryChargeSwitch.setChecked(chargeStatus != null && (int) chargeStatus > 0);
            }

            String batteryTemperature =
                    (attributes.getBatteryTemperature() != null) ? this.toTemperatureString(
                            attributes.getBatteryTemperature()) : labelVoid;
            this.batteryTemperatureTextView.setText(batteryTemperature);

            String batteryAutonomy =
                    (attributes.getBatteryAutonomy() != null) ? this.toOdometerString(attributes
                            .getBatteryAutonomy()
                            .doubleValue()) : labelVoid;
            this.batteryAutonomyTextView.setText(batteryAutonomy);

            int visibility = View.GONE;
            if (attributes.getBatteryCapacity() != null) {
                this.batteryCapacityTextView.setText(
                        String.format("%d kW/h", attributes.getBatteryCapacity()));
                visibility = attributes.getBatteryCapacity() > 0 ? View.VISIBLE : View.GONE;
            }
            this.capacityRow.setVisibility(visibility);

            String batteryEnergy =
                    attributes.getBatteryAvailableEnergy() != null ? String.format("%d kW/h",
                            attributes.getBatteryAvailableEnergy()) : labelVoid;
            this.batteryEnergyTextView.setText(batteryEnergy);

            String batteryRemainingTime =
                    (attributes.getChargingRemainingTime() != null) ? String.format("%d m",
                            attributes.getChargingRemainingTime()) : labelVoid;
            this.batteryRemainingTimeTextView.setText(batteryRemainingTime);
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
        boolean isMiles = this.defaultSharedPreferences.getBoolean("units_distance_miles", false);
        Measure kilometers = new Measure(value, MeasureUnit.KILOMETER);
        Measure miles = new Measure(value / 1.60934, MeasureUnit.MILE);
        MeasureFormat formatter =
                MeasureFormat.getInstance(Tools.getSystemLocale(), MeasureFormat.FormatWidth.SHORT);
        return formatter.formatMeasures((isMiles) ? miles : kilometers);
    }

    /**
     * I18N method to produce a temperature string. Will differentiate between °C and °F
     *
     * @param value to be displayed
     * @return computed string
     */
    private String toTemperatureString(int value) {
        boolean isFahrenheit =
                this.defaultSharedPreferences.getBoolean("units_temperature_fahrenheit", false);
        Measure celsius = new Measure(value, MeasureUnit.CELSIUS);
        Measure fahrenheit = new Measure(value * 9 / 5 + 32, MeasureUnit.FAHRENHEIT);
        MeasureFormat formatter =
                MeasureFormat.getInstance(Tools.getSystemLocale(), MeasureFormat.FormatWidth.SHORT);
        return formatter.formatMeasures((isFahrenheit) ? fahrenheit : celsius);
    }

    /**
     * Worker for triggering the update from swipe2refresh as well as from menu action.
     */
    private void triggerUpdate() {
        this.homeViewModel.updateCockpit(this.currentVin);
        this.homeViewModel.updateBatteryStatus(this.currentVin);
    }
}