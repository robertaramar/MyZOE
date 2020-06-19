package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.battery.Attributes;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object for Kamereon BatteryStatus.
 */
@Data
@ToString
@Builder
public class BatteryData {
    /**
     * null if okay, != null if error occurred.
     */
    private Throwable throwable;

    /**
     * Battery status information.
     */
    private Attributes attributes;
}
