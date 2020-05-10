package de.aramar.zoe.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import de.aramar.zoe.data.Summary;
import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.KamereonClient;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = HomeViewModel.class.getCanonicalName();

    private static final String BATTERY_STATUS =
            "http://10.0.2.2:55555/commerce/v1/accounts/bd69aaf9-7e7e-4fa6-b6a9-88b92f188cab" + "/kamereon/kca/car-adapter/v2/cars/VF1AG000X64494657/battery-status?country=GB";

    /**
     * Access to Kamereon API.
     */
    private KamereonClient kamereonClient;

    // Provided live data
    private MutableLiveData<Summary> mSummary;

    private Summary summary = new Summary();

    // Subscribed live data
    private LiveData<Vehicles> mVehicles;

    private LiveData<BatteryStatus> mBatteryResponse;

    private LiveData<Cockpit> mCockpitResponse;

    private LiveData<Location> mLocationRespose;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.mSummary = new MutableLiveData<>();
        this.mSummary.setValue(this.summary);

        this.kamereonClient = KamereonClient.getKamereonClient(application);
        this.mVehicles = this.kamereonClient.getVehiclesLiveData();
        this.mBatteryResponse = this.kamereonClient.getBatteryResponseLiveData();
        this.mBatteryResponse.observeForever(new Observer<BatteryStatus>() {
            @Override
            public void onChanged(BatteryStatus batteryResponse) {
                HomeViewModel.this.summary.setBattery(batteryResponse);
                HomeViewModel.this.mSummary.postValue(HomeViewModel.this.summary);
            }
        });
        this.mCockpitResponse = this.kamereonClient.getCockpitResponseLiveData();
        this.mCockpitResponse.observeForever(new Observer<Cockpit>() {
            @Override
            public void onChanged(Cockpit cockpitResponse) {
                HomeViewModel.this.summary.setCockpit(cockpitResponse);
                HomeViewModel.this.mSummary.postValue(HomeViewModel.this.summary);
            }
        });
        this.mLocationRespose = this.kamereonClient.getLocationResponseLiveData();
    }

    public LiveData<Summary> getSummary() {
        return this.mSummary;
    }

    public LiveData<Vehicles> getVehicles() {
        return this.mVehicles;
    }

    public LiveData<Location> getLocation() {
        return this.mLocationRespose;
    }

    public void updateBatteryStatus(String vin) {
        this.kamereonClient.getBatteryStatus(vin);
    }

    public void updateCockpit(String vin) {
        this.kamereonClient.getCockpit(vin);
    }

    public void updateLocation(String vin) {
        this.kamereonClient.getLocation(vin);
    }
}
