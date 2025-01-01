package utep.ilink.dropwizardtemplate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Output {
    @JsonProperty("outputs")
    private Variable[] variables;

    public Output(){

    }

    public Output(Variable[] variables){
        this.variables = variables;
    }

    public Variable[] getVariables(){
        return variables;
    }
}
