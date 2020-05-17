package de.aramar.zoe.data;

import java.time.Instant;
import java.util.List;

import lombok.Data;

/**
 * DTO to hold location information.
 *
 * @author robert.schneider@aramar.de
 */
@Data
class LocationResponse {

    @Data
    private static class LocationAttributes {
        /**
         * Latitude.
         */
        private float gpsLatitude;

        /**
         * Longitude.
         */
        private float gpsLongitude;

        /**
         * Timestamp of last update.
         */
        private Instant lastUpdateTime;
    }

    /**
     * Only available in case of errors;
     */
    List<Error> errors;

    /**
     * The payload data.
     */
    LocationAttributes data;
}
