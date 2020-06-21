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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.aramar.zoe.R;
import de.aramar.zoe.data.BatteryData;
import de.aramar.zoe.data.ChargeData;
import de.aramar.zoe.data.CockpitData;
import de.aramar.zoe.data.HvacData;
import de.aramar.zoe.data.kamereon.battery.Attributes;
import de.aramar.zoe.data.kamereon.battery.ChargeStateEnum;
import de.aramar.zoe.data.kamereon.battery.PlugStateEnum;
import de.aramar.zoe.data.kamereon.hvac.HvacCommandEnum;
import de.aramar.zoe.data.kamereon.vehicles.Asset;
import de.aramar.zoe.data.kamereon.vehicles.VehicleLink;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.BackendTraffic;
import de.aramar.zoe.utilities.Tools;
import lombok.SneakyThrows;


public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private static final String TAG = HomeFragment.class.getCanonicalName();

    private HomeViewModel homeViewModel;

    private TextView odometerValueTextView;

    private TextView batteryTimestampValue;

    private TextView batteryValueTextView;

    private Spinner finSpinner;

    private Switch batteryPlugSwitch;

    private Switch batteryChargeSwitch;

    private TextView batteryChargeSwitchText;

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

    private View batteryTimestampRow;

    private TextView batteryPlugSwitchText;

    private Button buttonAirCondition;

    private Button buttonCharge;

    private Boolean hvacStatus;

    private View cockpitRow;

    private View cockpitTitleRow;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        this.imageLoader = BackendTraffic.getInstance(this.getContext())
                .getImageLoader();

        this.homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        this.homeViewModel.getVehicles()
                .observe(this.getViewLifecycleOwner(), // Disable if there is only one entry
                        this::onVehicles);
        this.homeViewModel.getBatteryData()
                .observe(this.getViewLifecycleOwner(), this::onBatteryData);
        this.homeViewModel.getCockpitData()
                .observe(this.getViewLifecycleOwner(), this::onCockpitData);
        this.homeViewModel.getHvacData()
                .observe(this.getViewLifecycleOwner(), this::onHvacData);
        this.homeViewModel.getChargeData()
                .observe(this.getViewLifecycleOwner(), this::onChargeData);
        this.swipeRefreshLayout.setRefreshing(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        this.cockpitRow = root.findViewById(R.id.cockpit_row);
        this.cockpitTitleRow = root.findViewById(R.id.cockpit_title_row);
        this.odometerValueTextView = root.findViewById(R.id.odometer_value);
        this.batteryTimestampValue = root.findViewById(R.id.battery_timestamp_value);
        this.batteryTimestampRow = root.findViewById(R.id.battery_timestamp_row);
        this.batteryValueTextView = root.findViewById(R.id.battery_level_value);
        this.batteryPlugSwitch = root.findViewById(R.id.battery_plug_switch);
        this.batteryPlugSwitch.setClickable(false);
        this.batteryPlugSwitchText = root.findViewById(R.id.battery_plug_switch_text);
        this.batteryChargeSwitch = root.findViewById(R.id.battery_charge_switch);
        this.batteryChargeSwitch.setOnClickListener(this);
        this.batteryChargeSwitch.setClickable(false);
        this.batteryChargeSwitchText = root.findViewById(R.id.battery_charge_switch_text);
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

        this.buttonAirCondition = root.findViewById(R.id.button_air_condition_on);
        this.buttonAirCondition.setOnClickListener(this);
        this.buttonCharge = root.findViewById(R.id.button_air_condition_off);
        this.buttonCharge.setOnClickListener(this);

        return root;
    }

    @SneakyThrows
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Log.i(TAG, "Got an item change to position = " + position + " id = " + id);
        Object item = adapterView.getAdapter()
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_air_condition_on:
                this.homeViewModel.sendHvacCommand(this.currentVin, HvacCommandEnum.START);
                break;
            case R.id.button_air_condition_off:
                this.homeViewModel.sendHvacCommand(this.currentVin, HvacCommandEnum.STOP);
                break;
            case R.id.battery_charge_switch:
                this.homeViewModel.sendChargeCommand(this.currentVin);
                break;
            default:
                break;
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
        this.triggerUpdate();
        // Try to get the image of the vehicle
        List<Asset> assets = vehicle.getVehicleDetails()
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
     * @param batteryData Received vehicle battery data
     */
    @SuppressLint({"DefaultLocale"})
    private void updateBatteryData(BatteryData batteryData) {
        String labelVoid = this.requireContext()
                .getResources()
                .getString(R.string.label_void);
        Attributes attributes = batteryData.getAttributes();

        if (attributes.getTimestamp() != null) {
            this.batteryTimestampValue.setText(Tools.getLocalizedTimestamp(attributes.getTimestamp()));
            this.batteryTimestampRow.setVisibility(View.VISIBLE);
        } else {
            this.batteryTimestampRow.setVisibility(View.GONE);
        }

        String batteryLevel = (attributes.getBatteryLevel() != null) ? String.format("%d%%",
                attributes.getBatteryLevel()) : labelVoid;
        this.batteryValueTextView.setText(batteryLevel);
        this.batteryProgressBar.setProgress(attributes.getBatteryLevel() != null ? attributes.getBatteryLevel() : 0);

        this.setChargeState(attributes.getChargingStatus(), attributes.getPlugStatus(),
                attributes.getAdditionalProperties());

        String batteryTemperature = (attributes.getBatteryTemperature() != null) ? this.toTemperatureString(
                attributes.getBatteryTemperature()) : labelVoid;
        this.batteryTemperatureTextView.setText(batteryTemperature);

        String batteryAutonomy = (attributes.getBatteryAutonomy() != null) ? this.toOdometerString(
                attributes.getBatteryAutonomy()
                        .doubleValue()) : labelVoid;
        this.batteryAutonomyTextView.setText(batteryAutonomy);

        int visibility = View.GONE;
        if (attributes.getBatteryCapacity() != null) {
            this.batteryCapacityTextView.setText(String.format("%d kWh", attributes.getBatteryCapacity()));
            visibility = attributes.getBatteryCapacity() > 0 ? View.VISIBLE : View.GONE;
        }
        this.capacityRow.setVisibility(visibility);

        String batteryEnergy = attributes.getBatteryAvailableEnergy() != null ? String.format("%d kWh",
                attributes.getBatteryAvailableEnergy()) : labelVoid;
        this.batteryEnergyTextView.setText(batteryEnergy);

        String batteryRemainingTime = (attributes.getChargingRemainingTime() != null) ? String.format("%d m",
                attributes.getChargingRemainingTime()) : labelVoid;
        this.batteryRemainingTimeTextView.setText(batteryRemainingTime);
    }

    /**
     * Transfer received vehicle data into the UI elements.
     *
     * @param cockpitData Received vehicle cockpit data
     */
    private void updateCockpitData(CockpitData cockpitData) {
        if (this.defaultSharedPreferences.getBoolean("cmd_cockpit", true)) {
            this.cockpitRow.setVisibility(View.VISIBLE);
            this.cockpitTitleRow.setVisibility(View.VISIBLE);
            this.odometerValueTextView.setText(this.toOdometerString(cockpitData.getAttributes()
                    .getTotalMileage()));
        }
    }

    private void setChargeState(Double chargingStatus, Integer plugStatus, Map<String, Object> additionalProperties) {
        PlugStateEnum plugStateEnum = PlugStateEnum.getEnumFromValue(plugStatus);
        this.batteryPlugSwitch.setChecked(plugStateEnum.isPlugged());
        this.batteryPlugSwitchText.setText(plugStateEnum.getStateDescription());

        if (chargingStatus != null) {
            // V2 API
            ChargeStateEnum chargeStateEnum = ChargeStateEnum.getEnumFromValue(chargingStatus);
            this.batteryChargeSwitch.setChecked(chargeStateEnum.isCharging());
            this.batteryChargeSwitchText.setText(chargeStateEnum.getStateDescription());
            this.batteryChargeSwitch.setClickable(plugStateEnum.isPlugged() && !chargeStateEnum.isCharging());
        } else {
            // V1 API
            Object chargeStatus = null;
            if (additionalProperties.containsKey("chargeStatus")) {
                chargeStatus = additionalProperties.get("chargeStatus");
            }
            this.batteryChargeSwitch.setChecked(chargeStatus != null && (int) chargeStatus > 0);
            this.batteryChargeSwitch.setClickable(
                    plugStateEnum.isPlugged() && chargeStatus != null && (int) chargeStatus == 0);
        }
    }

    /**
     * Worker for triggering the update from swipe2refresh as well as from menu action.
     */
    private void triggerUpdate() {
        if (this.defaultSharedPreferences.getBoolean("cmd_cockpit", true)) {
            this.homeViewModel.updateCockpit(this.currentVin);
        } else {
            this.cockpitTitleRow.setVisibility(View.GONE);
            this.cockpitRow.setVisibility(View.GONE);
        }
        this.homeViewModel.updateBatteryStatus(this.currentVin);
    }

    private void onChargeData(ChargeData chargeData) {
        if (chargeData.getThrowable() == null) {
            String command = chargeData.getAttributes()
                    .getAction();
            String toastText = String.format(this.getString(R.string.toast_charge_good), command);
            Toast.makeText(this.getContext(), toastText, Toast.LENGTH_LONG)
                    .show();
            this.triggerUpdate();
        } else {
            Tools.displayError(chargeData.getThrowable(), this.getContext());
        }
    }

    private void onHvacData(HvacData hvacData) {
        if (hvacData.getThrowable() == null) {
            int targetTemperature = hvacData.getAttributes()
                    .getTargetTemperature();
            String command = hvacData.getAttributes()
                    .getAction();
            String toastText =
                    String.format(this.getString(R.string.toast_aircondition_good), command, targetTemperature);
            Toast.makeText(this.getContext(), toastText, Toast.LENGTH_LONG)
                    .show();
        } else {
            Tools.displayError(hvacData.getThrowable(), this.getContext());
        }
    }

    private void onCockpitData(CockpitData cockpitData) {
        if (cockpitData.getThrowable() == null) {
            this.updateCockpitData(cockpitData);
        } else {
            Tools.displayError(cockpitData.getThrowable(), this.getContext());
        }
        if (this.swipeRefreshLayout.isRefreshing()) {
            this.swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onBatteryData(BatteryData batteryData) {
        if (batteryData.getThrowable() == null) {
            this.updateBatteryData(batteryData);
        } else {
            Tools.displayError(batteryData.getThrowable(), this.getContext());
        }
        if (this.swipeRefreshLayout.isRefreshing()) {
            this.swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Extract required information from the vehicles data.
     *
     * @param vehicles Kamereon vehicles data
     */
    private void onVehicles(Vehicles vehicles) {
        // Filter out all non-ZOE vehicles
        Object[] zoes = Objects.requireNonNull(vehicles.getVehicleLinks()
                .stream()
                .filter(vehicleLink -> vehicleLink.getVehicleDetails()
                        .getModel()
                        .getLabel()
                        .compareToIgnoreCase("ZOE") == 0)
                .toArray());
        // if there is/are ZOE(s)
        if (zoes.length > 0) {
            ArrayAdapter<Object> vehiclesArrayAdapter =
                    new ArrayAdapter<>(this.requireContext(), android.R.layout.simple_spinner_dropdown_item, zoes);
            vehiclesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.finSpinner.setAdapter(vehiclesArrayAdapter);
            if (zoes.length > 1) {
                this.finSpinner.setEnabled(true);
                this.finSpinner.setVisibility(View.VISIBLE);
            } else {
                if (this.defaultSharedPreferences.getBoolean("ui_show_vin_spinner", false)) {
                    this.finSpinner.setVisibility(View.VISIBLE);
                    this.finSpinner.setEnabled(false);
                } else {
                    // Disable if there is only one entry
                    this.finSpinner.setVisibility(View.GONE);
                    this.changeCar((VehicleLink) zoes[0]);
                }
            }
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
        MeasureFormat formatter = MeasureFormat.getInstance(Tools.getSystemLocale(), MeasureFormat.FormatWidth.SHORT);
        return formatter.formatMeasures((isMiles) ? miles : kilometers);
    }

    /**
     * I18N method to produce a temperature string. Will differentiate between °C and °F
     *
     * @param value to be displayed
     * @return computed string
     */
    private String toTemperatureString(int value) {
        boolean isFahrenheit = this.defaultSharedPreferences.getBoolean("units_temperature_fahrenheit", false);
        Measure celsius = new Measure(value, MeasureUnit.CELSIUS);
        Measure fahrenheit = new Measure(value * 9 / 5 + 32, MeasureUnit.FAHRENHEIT);
        MeasureFormat formatter = MeasureFormat.getInstance(Tools.getSystemLocale(), MeasureFormat.FormatWidth.SHORT);
        return formatter.formatMeasures((isFahrenheit) ? fahrenheit : celsius);
    }
}