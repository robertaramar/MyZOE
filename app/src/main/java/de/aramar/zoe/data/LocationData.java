package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.location.Attributes;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object for Kamereon Location.
 */
@Data
@ToString
@Builder
public class LocationData {
    /**
     * null if okay, != null if error occurred.
     */
    private Throwable throwable;

    /**
     * Location information.
     */
    private Attributes attributes;
}
