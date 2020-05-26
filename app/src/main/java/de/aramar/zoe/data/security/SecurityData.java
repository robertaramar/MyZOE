package de.aramar.zoe.data.security;

import com.auth0.android.jwt.JWT;

import java.util.Locale;

import lombok.Data;

@Data
public class SecurityData {

    /**
     * Just some text.
     */
    private String text = "";

    /**
     * Current status of the security container.
     */
    private SecurityStatus status = SecurityStatus.EMPTY;

    /**
     * ConfigData (Kamereon) Wired API key.
     */
    private String wiredApiKey;

    /**
     * Current Kamereon JWT.
     */
    private String kamereonJwt;

    /**
     * The Kamereon account ID of the MYRENAULT account.
     */
    private String accountId;

    /**
     * The Gigya API URL.
     */
    private String gigyaTarget;

    /**
     * The Gigya person ID.
     */
    private String gigyaPersonId;

    /**
     * ConfigData Gigya session token key.
     */
    private String gigyaSessionToken;

    /**
     * ConfigData Gigya API key.
     */
    private String gigyaApiKey;

    /**
     * Current Gigya JWT.
     */
    private String gigyaJwt;

    /**
     * The locale for the user.
     */
    private Locale locale;

    /**
     * The wired API URL.
     */
    private String wiredTarget;

    /**
     * Test for Gigya JWT expiration.
     *
     * @return true if expired or expires within 10 seconds
     */
    public boolean isGigyaJwtExpired() {
        if (this.gigyaJwt != null) {
            JWT gigyaJWT = new JWT(this.gigyaJwt);
            return gigyaJWT.isExpired(10);
        } else {
            return true;
        }
    }

    /**
     * Test for Kamereon JWT expiration.
     *
     * @return true if expired or expires within 10 seconds
     */
    public boolean isKamereonJwtExpired() {
        if (this.kamereonJwt != null) {
            JWT kamereonJWT = new JWT(this.kamereonJwt);
            return kamereonJWT.isExpired(10);
        } else {
            return true;
        }
    }

    /**
     * Test for either Gigya or Kamereon JWT expiration.
     *
     * @return true if either JWT is expired
     */
    public boolean isJwtExpired() {
        return this.isGigyaJwtExpired() || this.isKamereonJwtExpired();
    }


    /**
     * Utility method to append a text to the current text.
     *
     * @param newText text to be appended
     */
    public void appendText(String newText) {
        this.text = this.text + "\n" + newText;
    }

    /**
     * Clear the security data;
     */
    public void clear() {
        this.status = SecurityStatus.EMPTY;
        this.text = "";
        this.gigyaJwt = null;
        this.kamereonJwt = null;
    }
}
