package de.aramar.zoe.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import de.aramar.zoe.data.Summary;
import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.KamereonClient;

public class HomeViewModel extends AndroidViewModel {

    /**
     * Access to Kamereon API.
     */
    private KamereonClient kamereonClient;

    // Provided live data
    private MutableLiveData<Summary> mSummary;

    private Summary summary = new Summary();

    // Subscribed live data
    private LiveData<Vehicles> mVehicles;

    private LiveData<Location> mLocationRespose;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.mSummary = new MutableLiveData<>();
        this.mSummary.setValue(this.summary);

        this.kamereonClient = KamereonClient.getKamereonClient(application);
        this.mVehicles = this.kamereonClient.getVehiclesLiveData();
        LiveData<BatteryStatus> mBatteryResponse = this.kamereonClient.getBatteryResponseLiveData();
        mBatteryResponse.observeForever(batteryResponse -> {
            HomeViewModel.this.summary.setBattery(batteryResponse);
            HomeViewModel.this.mSummary.postValue(HomeViewModel.this.summary);
        });
        LiveData<Cockpit> mCockpitResponse = this.kamereonClient.getCockpitResponseLiveData();
        mCockpitResponse.observeForever(cockpitResponse -> {
            HomeViewModel.this.summary.setCockpit(cockpitResponse);
            HomeViewModel.this.mSummary.postValue(HomeViewModel.this.summary);
        });
        this.mLocationRespose = this.kamereonClient.getLocationResponseLiveData();
    }

    LiveData<Summary> getSummary() {
        return this.mSummary;
    }

    public LiveData<Vehicles> getVehicles() {
        return this.mVehicles;
    }

    public LiveData<Location> getLocation() {
        return this.mLocationRespose;
    }

    void updateBatteryStatus(String vin) {
        this.kamereonClient.getBatteryStatus(vin);
    }

    void updateCockpit(String vin) {
        this.kamereonClient.getCockpit(vin);
    }

    public void updateLocation(String vin) {
        this.kamereonClient.getLocation(vin);
    }
}
