package de.aramar.zoe.data.security;

import java.util.Locale;

import lombok.Data;
import lombok.ToString;

/**
 * Data obtained from config_<country>.json.
 */
@Data
@ToString
public class ConfigData {
    /**
     * They WIRED target URL, obtained from /config_<country>.json.
     * Contained in servers.target
     */
    private String wiredTarget;

    /**
     * They WIRED API key, obtained from /config_<country>.json.
     * Contained in servers.wiredProd
     */
    private String wiredApiKey;

    /**
     * The GIGYA target URL, obtained from /config_<country>.json.
     * Contained in servers.target.
     */
    private String gigyaTarget;

    /**
     * The GIGYA API key, obtained from /config_<country>.json.
     * Contained in servers.gigyaProd.
     */
    private String gigyaApiKey;

    /**
     * User's locale, can be used to construct country and language.
     */
    private Locale locale;

    /**
     * Set if an error occurred retrieving the data.
     */
    private boolean error;

    /**
     * Description, log-message of last error.
     */
    private String errorText;

    /**
     * Check whether this is a valid config.
     *
     * @return true if valid
     */
    public boolean isValid() {
        return this.locale != null && this.wiredApiKey != null && this.wiredTarget != null && this.gigyaApiKey != null && this.gigyaTarget != null;
    }
}
