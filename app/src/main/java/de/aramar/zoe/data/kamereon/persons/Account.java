package de.aramar.zoe.data.kamereon.persons;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Account {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("accountStatus")
    private String accountStatus;

    @JsonProperty("country")
    private String country;

    @JsonProperty("personId")
    private String personId;

    @JsonProperty("relationType")
    private String relationType;

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
