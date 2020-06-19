package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.cockpit.Attributes;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object for Kamereon Cockpit.
 */
@Data
@ToString
@Builder
public class CockpitData {
    /**
     * null if okay, != null if error occurred.
     */
    private Throwable throwable;

    /**
     * Cockpit information.
     */
    private Attributes attributes;
}
