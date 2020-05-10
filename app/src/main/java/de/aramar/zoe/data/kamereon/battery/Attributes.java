package de.aramar.zoe.data.kamereon.battery;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Attributes {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("batteryLevel")
    private Integer batteryLevel;

    @JsonProperty("batteryTemperature")
    private Integer batteryTemperature;

    @JsonProperty("batteryAutonomy")
    private Integer batteryAutonomy;

    @JsonProperty("batteryCapacity")
    private Integer batteryCapacity;

    @JsonProperty("batteryAvailableEnergy")
    private Integer batteryAvailableEnergy;

    @JsonProperty("plugStatus")
    private Integer plugStatus;

    @JsonProperty("chargingStatus")
    private Double chargingStatus;

    @JsonProperty("chargingRemainingTime")
    private Integer chargingRemainingTime;

    @JsonProperty("chargingInstantaneousPower")
    private Double chargingInstantaneousPower;

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
