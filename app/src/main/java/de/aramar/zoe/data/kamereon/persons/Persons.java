package de.aramar.zoe.data.kamereon.persons;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Persons {

    @JsonProperty("personId")
    private String personId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("country")
    private String country;

    @JsonProperty("civility")
    private String civility;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("idp")
    private Idp idp;

    @JsonProperty("emails")
    private List<Email> emails = null;

    @JsonProperty("identities")
    private List<Object> identities = null;

    @JsonProperty("myrRequest")
    private Boolean myrRequest;

    @JsonProperty("accounts")
    private List<Account> accounts = null;

    @JsonProperty("partyId")
    private String partyId;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("lastModifiedDate")
    private String lastModifiedDate;

    @JsonProperty("functionalCreationDate")
    private String functionalCreationDate;

    @JsonProperty("functionalModificationDate")
    private String functionalModificationDate;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("originApplicationName")
    private String originApplicationName;

    @JsonProperty("originUserId")
    private String originUserId;

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
