package de.aramar.zoe.data.kamereon.vehicles;

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
public class VehicleLink {

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("vin")
    private String vin;

    @JsonProperty("status")
    private String status;

    @JsonProperty("linkType")
    private String linkType;

    @JsonProperty("garageBrand")
    private String garageBrand;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("lastModifiedDate")
    private String lastModifiedDate;

    @JsonProperty("cancellationReason")
    private CancellationReason cancellationReason;

    @JsonProperty("connectedDriver")
    private ConnectedDriver connectedDriver;

    @JsonProperty("vehicleDetails")
    private VehicleDetails vehicleDetails;

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

    @Override
    public String toString() {
        return this.vin + " - " + this.vehicleDetails
                .getBrand()
                .getLabel() + " - " + this.vehicleDetails
                .getModel()
                .getLabel();
    }
}
