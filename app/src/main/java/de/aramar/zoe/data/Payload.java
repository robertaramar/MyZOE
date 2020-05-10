package de.aramar.zoe.data;

import lombok.Data;

@Data
public class Payload<T> {
    /**
     * The type of vehicle (e.g. "car")
     */
    private String type;

    /**
     * The ID of the vehicle (e.g. FIN)
     */
    private String id;

    /**
     * The real payload.
     */
    T attributes;
}
