package de.aramar.zoe.data;

import lombok.Data;

/**
 * Class that holds error information if REST call failed.
 */
@Data
class Error {
    /**
     * HTTP status code.
     */
    private String status;

    /**
     * Code detailing the error type.
     */
    private String code;

    /**
     * Even more details.
     */
    private String detail;
}
