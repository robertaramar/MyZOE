package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.hvac.HvacCommandEnum;
import lombok.Data;

/**
 * Summary DTO containing all three ZOE DTOs.
 */
@Data
public class Summary {
    /**
     * ZOE's battery.
     */
    private BatteryStatus battery;

    /**
     * ZOE's cockpit.
     */
    private Cockpit cockpit;

    /**
     * Last HVAC command sent.
     */
    private HvacCommandEnum hvacCommand;

    /**
     * Indicator if heating was remotely started.
     * null = no command has been issued, yet
     * true = start command successfully sent
     * false = start failed, or cancel succeeded
     */
    private Boolean hvacStatus;
}
