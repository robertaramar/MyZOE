package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.hvac.Attributes;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object for Kamereon HVAC commands.
 */
@Data
@ToString
@Builder
public class HvacData {
    /**
     * null if okay, != null if error occurred.
     */
    private Throwable throwable;

    /**
     * HVAC information.
     */
    private Attributes attributes;
}
