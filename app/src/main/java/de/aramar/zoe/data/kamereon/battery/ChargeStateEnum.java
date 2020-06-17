package de.aramar.zoe.data.kamereon.battery;

import java.util.Arrays;

import de.aramar.zoe.R;
import lombok.Getter;

@Getter
public enum ChargeStateEnum {

    NOT_IN_CHARGE(0.0, false, R.string.charge_state_not_in_charge), //
    WAITING_FOR_PLANNED_CHARGE(0.1, false, R.string.charge_state_waiting_planned), //
    CHARGE_ENDED(0.2, false, R.string.charge_state_charge_ended), //
    WAITING_FOR_CURRENT_CHARGE(0.3, false, R.string.charge_state_waiting_current), //
    ENERGY_FLAP_OPENED(0.4, false, R.string.charge_state_flap_open), //
    CHARGE_IN_PROGRESS(1.0, true, R.string.charge_state_in_progress), //
    // This next is more accurately "not charging" (<= ZE40) or "error" (ZE50).
    // But I don't want to include 'error' in the output text because people will
    // think that it's an error in Pyze when their ZE40 isn't plugged in...
    CHARGE_ERROR(-1.0, false, R.string.charge_state_error), //
    NOT_AVAILABLE(-1.1, false, R.string.charge_state_not_available);

    private double stateValue;

    private boolean charging;

    private int stateDescription;

    /**
     * Constructor.
     *
     * @param stateValue       a weird float value
     * @param charging         is it charging?
     * @param stateDescription a text describing the state
     */
    ChargeStateEnum(double stateValue, boolean charging, int stateDescription) {
        this.stateValue = stateValue;
        this.charging = charging;
        this.stateDescription = stateDescription;
    }

    /**
     * Produce an enum from a value.
     *
     * @param stateValue value to looked up
     * @return enum that is found, NOT_AVAILABLE if not found
     */
    public static ChargeStateEnum getEnumFromValue(double stateValue) {
        return Arrays.asList(ChargeStateEnum.values())
                     .stream()
                     .filter(chargeStateEnum -> chargeStateEnum.stateValue == stateValue)
                     .findFirst()
                     .orElse(NOT_AVAILABLE);
    }
}
