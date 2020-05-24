package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
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
}
