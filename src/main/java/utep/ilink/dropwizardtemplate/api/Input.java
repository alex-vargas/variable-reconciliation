package utep.ilink.dropwizardtemplate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Input {
    @JsonProperty("inputs")
    private Variable[] variables;

    public Input(){

    }

    public Input(Variable[] variables){
        this.variables = variables;
    }

    public Variable[] getVariables(){
        return variables;
    }
}
