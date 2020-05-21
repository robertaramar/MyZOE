package de.aramar.zoe.data.kamereon.battery;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Getter
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

    /**
     * _CHARGE_STATES = {
     * 0.0: ['Not charging', 'NOT_IN_CHARGE'],
     * 0.1: ['Waiting for planned charge', 'WAITING_FOR_PLANNED_CHARGE'],
     * 0.2: ['Charge ended', 'CHARGE_ENDED'],
     * 0.3: ['Waiting for current charge', 'WAITING_FOR_CURRENT_CHARGE'],
     * 0.4: ['Energy flap opened', 'ENERGY_FLAP_OPENED'],
     * 1.0: ['Charging', 'CHARGE_IN_PROGRESS'],
     * # This next is more accurately "not charging" (<= ZE40) or "error" (ZE50).
     * # But I don't want to include 'error' in the output text because people will
     * # think that it's an error in Pyze when their ZE40 isn't plugged in...
     * -1.0: ['Not charging or plugged in', 'CHARGE_ERROR'],
     * -1.1: ['Not available', 'NOT_AVAILABLE']
     * }
     */
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
