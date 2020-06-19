package de.aramar.zoe.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import de.aramar.zoe.data.BatteryData;
import de.aramar.zoe.data.ChargeData;
import de.aramar.zoe.data.CockpitData;
import de.aramar.zoe.data.HvacData;
import de.aramar.zoe.data.LocationData;
import de.aramar.zoe.data.kamereon.hvac.HvacCommandEnum;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.network.KamereonRx;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = HomeViewModel.class.getCanonicalName();

    /**
     * Access to Kamereon API.
     */
    private final KamereonRx kamereonRx;

    // Provided live data
    private MutableLiveData<BatteryData> mBatteryData;

    private MutableLiveData<CockpitData> mCockpitData;

    private MutableLiveData<LocationData> mLocationData;

    private MutableLiveData<HvacData> mHvacData;

    private MutableLiveData<ChargeData> mChargeData;

    private MutableLiveData<Vehicles> mVehicles;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.kamereonRx = KamereonRx.getKamereonRx(application);
        this.mBatteryData = new MutableLiveData<>();
        this.mCockpitData = new MutableLiveData<>();
        this.mVehicles = new MutableLiveData<>();
        this.mLocationData = new MutableLiveData<>();
        this.mHvacData = new MutableLiveData<>();
        this.mChargeData = new MutableLiveData<>();
    }

    public LiveData<BatteryData> getBatteryData() {
        return this.mBatteryData;
    }

    public LiveData<CockpitData> getCockpitData() {
        return this.mCockpitData;
    }

    public LiveData<HvacData> getHvacData() {
        return this.mHvacData;
    }

    public LiveData<ChargeData> getChargeData() {
        return this.mChargeData;
    }

    public LiveData<LocationData> getLocationData() {
        return this.mLocationData;
    }

    public LiveData<Vehicles> getVehicles() {
        this.kamereonRx.getVehicles()
                       .subscribeOn(Schedulers.io())
                       .subscribe(vehicles -> {
                           Log.d(TAG, "A new vehicles status " + vehicles);
                           this.mVehicles.postValue(vehicles);
                       }, error -> {
                           Log.d(TAG, "An error vehicles status", error);
                       });
        return this.mVehicles;
    }

    void updateBatteryStatus(String vin) {
        this.kamereonRx.getBatteryStatus(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(batteryResponse -> {
                           Log.d(TAG, "A new battery status " + batteryResponse);
                           this.mBatteryData.postValue(BatteryData.builder()
                                                                  .attributes(batteryResponse.getData()
                                                                                             .getAttributes())
                                                                  .build());
                       }, throwable -> {
                           Log.d(TAG, "An error battery status", throwable);
                           this.mBatteryData.postValue(BatteryData.builder()
                                                                  .throwable(throwable)
                                                                  .build());
                       });
    }

    void updateCockpit(String vin) {
        this.kamereonRx.getCockpit(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(cockpitResponse -> {
                           Log.d(TAG, "A new cockpit status " + cockpitResponse);
                           this.mCockpitData.postValue(CockpitData.builder()
                                                                  .attributes(cockpitResponse.getData()
                                                                                             .getAttributes())
                                                                  .build());
                       }, throwable -> {
                           Log.d(TAG, "An error cockpit status", throwable);
                           this.mCockpitData.postValue(CockpitData.builder()
                                                                  .throwable(throwable)
                                                                  .build());
                       });
    }

    void sendHvacCommand(String vin, HvacCommandEnum hvacCommandEnum) {
        this.kamereonRx.postHVAC(vin, hvacCommandEnum)
                       .subscribeOn(Schedulers.io())
                       .subscribe(hvacPackage -> {
                           Log.d(TAG, "A new hvac command " + hvacPackage);
                           this.mHvacData.postValue(HvacData.builder()
                                                            .attributes(hvacPackage.getData()
                                                                                   .getAttributes())
                                                            .build());
                       }, throwable -> {
                           Log.d(TAG, "An error hvac command", throwable);
                           this.mHvacData.postValue(HvacData.builder()
                                                            .throwable(throwable)
                                                            .build());
                       });
    }

    void sendChargeCommand(String vin) {
        this.kamereonRx.postCharge(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(chargePackage -> {
                           Log.d(TAG, "A new charge command " + chargePackage);
                           this.mChargeData.postValue(ChargeData.builder()
                                                                .attributes(chargePackage.getData()
                                                                                         .getAttributes())
                                                                .build());
                       }, throwable -> {
                           Log.d(TAG, "An error charge command", throwable);
                           this.mChargeData.postValue(ChargeData.builder()
                                                                .throwable(throwable)
                                                                .build());
                       });
    }

    public void updateLocation(String fin) {
        this.kamereonRx.getLocation(fin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(location -> {
                           Log.d(TAG, "A new location status " + location);
                           this.mLocationData.postValue(LocationData.builder()
                                                                    .attributes(location.getData()
                                                                                        .getAttributes())
                                                                    .build());
                       }, throwable -> {
                           Log.d(TAG, "An error location status ", throwable);
                           this.mLocationData.postValue(LocationData.builder()
                                                                    .throwable(throwable)
                                                                    .build());
                       });
    }
}
