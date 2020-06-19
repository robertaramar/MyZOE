package de.aramar.zoe.data.kamereon.error;

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
public class ErrorEnvelope {

    @JsonProperty("type")
    private String type;

    @JsonProperty("messages")
    private List<Message> messages = null;

    @JsonProperty("errors")
    private List<Error> errors = null;

    @JsonProperty("error_reference")
    private String errorReference;

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
