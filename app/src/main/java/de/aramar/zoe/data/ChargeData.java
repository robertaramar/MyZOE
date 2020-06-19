package de.aramar.zoe.data;

import de.aramar.zoe.data.kamereon.charge.Attributes;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object for Kamereon charge commands.
 */
@Data
@ToString
@Builder
public class ChargeData {
    /**
     * null if okay, != null if error occurred.
     */
    private Throwable throwable;

    /**
     * Charge information.
     */
    private Attributes attributes;
}
