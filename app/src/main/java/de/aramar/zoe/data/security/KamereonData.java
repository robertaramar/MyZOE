package de.aramar.zoe.data.security;

import de.aramar.zoe.data.kamereon.persons.Persons;
import de.aramar.zoe.data.kamereon.token.Token;
import lombok.Data;

/**
 * Security data from the Kamereon-Framework.
 */
@Data
public class KamereonData {

    /**
     * The status of this data object.
     */
    private KamereonStatus status = KamereonStatus.EMTPY;

    /**
     * The Kamereon persons, contains an array of Kamereon accounts.
     */
    private Persons persons;

    /**
     * The Kamereon token response.
     */
    private Token token;

    /**
     * The JWT obtained from Kamereon
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
        return this.jwt != null && this.persons != null && this.persons.getAccounts() != null;
    }

    public enum KamereonStatus {
        EMTPY, PERSON_AVAILABLE, JWT_AVAILABLE, JWT_REFRESHED, JWT_EXPIRED
    }
}
