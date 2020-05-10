package de.aramar.zoe.data.security;

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
     * Current Gigya JWT.
     */
    private String gigyaJwt;

    /**
     * Current Kamereon JWT.
     */
    private String kamereonJwt;

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
