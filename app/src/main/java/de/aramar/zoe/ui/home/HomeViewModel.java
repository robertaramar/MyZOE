package de.aramar.zoe.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import de.aramar.zoe.data.Summary;
import de.aramar.zoe.data.kamereon.charge.ChargePackage;
import de.aramar.zoe.data.kamereon.hvac.HvacCommandEnum;
import de.aramar.zoe.data.kamereon.hvac.HvacPackage;
import de.aramar.zoe.data.kamereon.location.Location;
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
    private MutableLiveData<Summary> mSummary;

    private MutableLiveData<Location> mLocation;

    private MutableLiveData<Vehicles> mVehicles;

    private MutableLiveData<HvacPackage> mHvacPackage;

    private MutableLiveData<ChargePackage> mChargePackage;

    private Summary summary = new Summary();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.kamereonRx = KamereonRx.getKamereonRx(application);
        this.mSummary = new MutableLiveData<>();
        this.mSummary.setValue(this.summary);
        this.mVehicles = new MutableLiveData<>();
        this.mLocation = new MutableLiveData<>();
        this.mHvacPackage = new MutableLiveData<>();
        this.mChargePackage = new MutableLiveData<>();
    }

    public LiveData<Summary> getSummary() {
        return this.mSummary;
    }

    public LiveData<HvacPackage> getHvacPackage() {
        return this.mHvacPackage;
    }

    public LiveData<ChargePackage> getChargePackage() {
        return this.mChargePackage;
    }

    public LiveData<Vehicles> getVehicles() {
        this.kamereonRx.getVehicles()
                       .subscribeOn(Schedulers.io())
                       .subscribe(vehicles -> {
                           Log.d(TAG, "A new vehicles status " + vehicles);
                           this.mVehicles.postValue(vehicles);
                       }, error -> {
                           Log.d(TAG, "An error vehicles status " + error);
                       });
        return this.mVehicles;
    }

    public LiveData<Location> getLocation() {
        return this.mLocation;
    }

    void updateBatteryStatus(String vin) {
        this.kamereonRx.getBatteryStatus(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(batteryResponse -> {
                           Log.d(TAG, "A new battery status " + batteryResponse);
                           this.summary.setBattery(batteryResponse);
                           this.mSummary.postValue(this.summary);
                       }, error -> {
                           Log.d(TAG, "An error battery status " + error);
                           this.mSummary.postValue(this.summary); // TODO introduce error values
                       });
    }

    void updateCockpit(String vin) {
        this.kamereonRx.getCockpit(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(cockpitResponse -> {
                           Log.d(TAG, "A new cockpit status " + cockpitResponse);
                           this.summary.setCockpit(cockpitResponse);
                           this.mSummary.postValue(this.summary);
                       }, error -> {
                           Log.d(TAG, "An error cockpit status " + error);
                           this.mSummary.postValue(this.summary); // TODO introduce error values
                       });
    }

    void sendHvacCommand(String vin, HvacCommandEnum hvacCommandEnum) {
        this.kamereonRx.postHVAC(vin, hvacCommandEnum)
                       .subscribeOn(Schedulers.io())
                       .subscribe(hvacPackage -> {
                           Log.d(TAG, "A new hvac command " + hvacPackage);
                           this.mHvacPackage.postValue(hvacPackage);
                       }, throwable -> {
                           Log.d(TAG, "An error hvac command", throwable);
                           HvacPackage hvacPackage = new HvacPackage();
                           hvacPackage.setData(null);
                           this.mHvacPackage.postValue(hvacPackage);
                       });
    }

    void sendChargeCommand(String vin) {
        this.kamereonRx.postCharge(vin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(chargePackage -> {
                           Log.d(TAG, "A new charge command " + chargePackage);
                           this.mChargePackage.postValue(chargePackage);
                       }, throwable -> {
                           Log.d(TAG, "An error charge command", throwable);
                           ChargePackage chargePackage = new ChargePackage();
                           chargePackage.setData(null);
                           this.mChargePackage.postValue(chargePackage);
                       });
    }

    public void updateLocation(String fin) {
        this.kamereonRx.getLocation(fin)
                       .subscribeOn(Schedulers.io())
                       .subscribe(location -> {
                           Log.d(TAG, "A new location status " + location);
                           this.mLocation.postValue(location);
                       }, error -> {
                           Log.d(TAG, "An error location status ", error);
                           this.mLocation.postValue(null);
                       });
    }
}
