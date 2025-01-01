package utep.ilink.dropwizardtemplate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssumptionIgnore {
    @JsonProperty("ignore")
    private String[] ignore;

    public String[] getIgnore() {
        return ignore;
    }

    public void setIgnore(String[] ignore) {
        this.ignore = ignore;
    }

    public AssumptionIgnore() {
    }

    public AssumptionIgnore(String[] ignore) {
        this.ignore = ignore;
    }
}