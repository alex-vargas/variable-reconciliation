package utep.ilink.dropwizardtemplate.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelCatalog {
    @JsonProperty("models")
    private Model[] models;

    public ModelCatalog(){

    }

    public ModelCatalog(Model[] models){
        this.models = models;
    }

    public Model[] getModels(){
        return models;
    }
}