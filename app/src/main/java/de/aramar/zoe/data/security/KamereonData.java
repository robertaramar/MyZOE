package de.aramar.zoe.data.security;

import com.auth0.android.jwt.JWT;

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
     * Indicates an error when retrieving data.
     */
    private boolean error;

    /**
     * Description, log entry of last error.
     */
    private String errorText;

    public boolean isJwtExpired() {
        if (this.token != null) {
            JWT kamereonJwt = new JWT(this.token.getAccessToken());
            return kamereonJwt.isExpired(10);
        } else {
            return true;
        }
    }


    public enum KamereonStatus {
        EMTPY, PERSON_AVAILABLE, JWT_AVAILABLE, JWT_REFRESHED
    }
}
