package de.aramar.zoe.data.security;

import de.aramar.zoe.R;

public enum SecurityStatus {
    /**
     * we are fresh
     */
    EMPTY(0, R.string.login_status_empty, R.string.login_status_empty),

    /**
     * the GIGYA and Kamereon tokens have been retrieved from config_xx_XX.json
     */
    API_TOKENS_AVAILABLE(1, R.string.login_status_ok_api_token,
            R.string.login_status_err_api_token),

    /**
     * gigya_session_available the user has been authenticated
     */
    GIGYA_SESSION_AVAILABLE(2, R.string.login_status_ok_gigya_session,
            R.string.login_status_err_gigya_session),

    /**
     * a valid JWT exists
     */
    GIGYA_JWT_AVAILABLE(3, R.string.login_status_ok_gigya_jwt, R.string.login_status_err_gigya_jwt),

    /**
     * person info retrieved
     */
    GIGYA_PERSON_AVAILABLE(4, R.string.login_status_ok_gigya_person,
            R.string.login_status_err_gigya_person),

    /**
     * The Kamereon person record (holding the accounts) is available
     */
    KAMEREON_PERSON_AVAILABLE(5, R.string.login_status_ok_kamereon_person,
            R.string.login_status_err_kamereon_person),

    /**
     * Kamereon token exists
     */
    KAMEREON_JWT_AVAILABLE(6, R.string.login_status_ok_kamereon_jwt,
            R.string.login_status_err_kamereon_jwt),

    /**
     * an error has occurred
     */
    ERROR(-1, R.string.login_value_status_error, R.string.login_value_status_error),

    /**
     * The current Gigya JWT is expired
     */
    GIGYA_JWT_EXPIRED(-2, R.string.login_status_expired_gigya, R.string.login_status_expired_gigya),

    /**
     * The current Gigya JWT is expired
     */
    KAMEREON_JWT_EXPIRED(-3, R.string.login_status_expired_kamereon,
            R.string.login_status_expired_kamereon);

    /**
     * The current level of security, the higher, the better
     */
    private int level;

    /**
     * Id for the textmodule, for good case.
     */
    private int textIdOk;

    /**
     * Id for the textmodule, for error case.
     */
    private int textIdErr;

    SecurityStatus(int level, int textIdOk, int textIdErr) {
        this.level = level;
        this.textIdOk = textIdOk;
        this.textIdErr = textIdErr;
    }

    public int getLevel() {
        return this.level;
    }

    public int getOkStatusTextId() {
        return this.textIdOk;
    }

    public int getErrorStatusTextId() {
        return this.textIdErr;
    }
}
