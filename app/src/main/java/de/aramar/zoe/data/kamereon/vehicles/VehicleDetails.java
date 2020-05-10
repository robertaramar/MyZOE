package de.aramar.zoe.data.kamereon.vehicles;

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
public class VehicleDetails {

    @JsonProperty("vin")
    private String vin;

    @JsonProperty("engineType")
    private String engineType;

    @JsonProperty("engineRatio")
    private String engineRatio;

    @JsonProperty("deliveryCountry")
    private DeliveryCountry deliveryCountry;

    @JsonProperty("family")
    private Family family;

    @JsonProperty("tcu")
    private Tcu tcu;

    @JsonProperty("navigationAssistanceLevel")
    private NavigationAssistanceLevel navigationAssistanceLevel;

    @JsonProperty("battery")
    private Battery battery;

    @JsonProperty("radioType")
    private RadioType radioType;

    @JsonProperty("registrationCountry")
    private RegistrationCountry registrationCountry;

    @JsonProperty("brand")
    private Brand brand;

    @JsonProperty("model")
    private Model model;

    @JsonProperty("gearbox")
    private Gearbox gearbox;

    @JsonProperty("version")
    private Version version;

    @JsonProperty("energy")
    private Energy energy;

    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("vcd")
    private String vcd;

    @JsonProperty("assets")
    private List<Asset> assets = null;

    @JsonProperty("yearsOfMaintenance")
    private Integer yearsOfMaintenance;

    @JsonProperty("connectivityTechnology")
    private String connectivityTechnology;

    @JsonProperty("easyConnectStore")
    private Boolean easyConnectStore;

    @JsonProperty("electrical")
    private Boolean electrical;

    @JsonProperty("rlinkStore")
    private Boolean rlinkStore;

    @JsonProperty("deliveryDate")
    private String deliveryDate;

    @JsonProperty("retrievedFromDhs")
    private Boolean retrievedFromDhs;

    @JsonProperty("engineEnergyType")
    private String engineEnergyType;

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
        return this.vin + " - " + this.brand.getLabel() + " - " + this.model.getLabel();
    }
}
