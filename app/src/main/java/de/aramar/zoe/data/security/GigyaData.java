package de.aramar.zoe.data.security;

import lombok.Data;

/**
 * Security data from the Gigya-Framework.
 */
@Data
public class GigyaData {

    /**
     * The status of this data object.
     */
    public GigyaStatus status = GigyaStatus.EMTPY;

    /**
     * Token from session login. https://accounts.eu1.gigya.com/accounts.login
     * Contained in sessionInfo.cookieValue
     */
    private String sessionCookie;

    /**
     * The person ID from  https://accounts.eu1.gigya.com/accounts.getAccountInfo.
     * Contained in data.personId
     */
    private String personId;

    /**
     * The JWT obtained from
     */
    private String jwt;

    /**
     * Indicates an error when retrieving data.
     */
    private boolean error;

    /**
     * Description, log entry of last error.
     */
    private String errorText;

    /**
     * Indicates a valid configuration.
     *
     * @return true if valid.
     */
    public boolean isValid() {
        return this.jwt != null && this.personId != null && this.sessionCookie != null;
    }

    public enum GigyaStatus {
        EMTPY, SESSION_COOKIE_AVAILABLE, PERSON_AVAILABLE, JWT_AVAILABLE, JWT_REFRESHED, JWT_EXPIRED
    }
}
