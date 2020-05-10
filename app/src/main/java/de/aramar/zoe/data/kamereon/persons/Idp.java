package de.aramar.zoe.data.kamereon.persons;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Idp {

    @JsonProperty("idpId")
    private String idpId;

    @JsonProperty("idpType")
    private String idpType;

    @JsonProperty("idpStatus")
    private String idpStatus;

    @JsonProperty("login")
    private String login;

    @JsonProperty("loginType")
    private String loginType;

    @JsonProperty("termsConditionAcceptance")
    private Boolean termsConditionAcceptance;

    @JsonProperty("termsConditionLastAcceptanceDate")
    private String termsConditionLastAcceptanceDate;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
