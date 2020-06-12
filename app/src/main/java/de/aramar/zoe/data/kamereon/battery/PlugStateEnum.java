package de.aramar.zoe.data.kamereon.battery;

import java.util.Arrays;

import de.aramar.zoe.R;
import lombok.Getter;

@Getter
public enum PlugStateEnum {

    UNPLUGGED(0, false, R.string.plug_state_unplugged), //
    PLUGGED(1, true, R.string.plug_state_plugged), //
    PLUG_ERROR(-1, false, R.string.plug_state_error), //
    NOT_AVAILABLE(-2147483648, false, R.string.plug_state_not_available);

    private int stateValue;

    private boolean plugged;

    private int stateDescription;

    /**
     * Constructor.
     *
     * @param stateValue       a weird int value
     * @param plugged          considered plugged?
     * @param stateDescription a text id describing the state
     */
    PlugStateEnum(int stateValue, boolean plugged, int stateDescription) {
        this.stateValue = stateValue;
        this.plugged = plugged;
        this.stateDescription = stateDescription;
    }

    /**
     * Produce an enum from a value.
     *
     * @param stateValue value to looked up
     * @return enum that is found, NOT_AVAILABLE if not found
     */
    public static PlugStateEnum getEnumFromValue(int stateValue) {
        return Arrays
                .asList(PlugStateEnum.values())
                .stream()
                .filter(plugStateEnum -> plugStateEnum.stateValue == stateValue)
                .findFirst()
                .orElse(NOT_AVAILABLE);
    }
}
