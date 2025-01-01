package utep.ilink.dropwizardtemplate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model {
    @JsonProperty("id")
    private String id;
    @JsonProperty("inputs")
    private Variable[] inputs;
    @JsonProperty("outputs")
    private Variable[] outputs;

    public Model(){

    }

    public Model(String id, Variable[] inputs, Variable[] outputs){
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Variable[] getInputs(){
        return inputs;
    }
    public Variable[] getOutputs(){
        return outputs;
    }
    public String getId(){
        return id;
    }
}