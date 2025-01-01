package utep.ilink.dropwizardtemplate.api;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("assumptions")
    private HashMap<String, String> assumptions;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public HashMap<String, String> getAssumptions() {
        return assumptions;
    }

    public Variable() {
    }

    public Variable(String id, String type, HashMap<String, String> assumptions) {
        this.id = id;
        this.type = type;
        this.assumptions = assumptions;
    }

    public String assumptionsToString(){
        if(assumptions == null || assumptions.size() == 0)
            return "No assumptions.";
        StringBuilder assumptionsString = new StringBuilder();
        for(String assumption : assumptions.keySet()){
            assumptionsString.append(" ");
            assumptionsString.append(assumption);
            assumptionsString.append(" - ");
            assumptionsString.append(assumptions.get(assumption));
        }
        return assumptionsString.toString();
    }

    public String toString() {

        return id + " - " + type + " - " + assumptionsToString();
    }

}
