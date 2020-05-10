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
public class Email {

    @JsonProperty("emailType")
    private String emailType;

    @JsonProperty("emailValue")
    private String emailValue;

    @JsonProperty("validityFlag")
    private Boolean validityFlag;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("lastModifiedDate")
    private String lastModifiedDate;

    @JsonProperty("functionalCreationDate")
    private String functionalCreationDate;

    @JsonProperty("functionalModificationDate")
    private String functionalModificationDate;

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
