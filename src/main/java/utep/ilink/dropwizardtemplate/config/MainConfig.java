package utep.ilink.dropwizardtemplate.config;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;
import io.federecio.dropwizard.swagger.*;

public class MainConfig extends Configuration {
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @NotEmpty
    private String ontologySource;

    @NotEmpty
    private String ontologySavePath;

    @NotEmpty
    private String iriPrefix;

    @NotEmpty
    private String equivalentClass;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty
    public String getOntologySource() {
        return ontologySource;
    }

    @JsonProperty
    public void setOntologySource(String owlSource) {
        this.ontologySource = owlSource;
    }

    @JsonProperty
    public String getOntologySavePath() {
        return ontologySavePath;
    }

    @JsonProperty
    public void setOntologySavePath(String owlSavePath) {
        this.ontologySavePath = owlSavePath;
    }

    @JsonProperty
    public String getIriPrefix() {
        return iriPrefix;
    }

    @JsonProperty
    public void setIriPrefix(String iriPrefix) {
        this.iriPrefix = iriPrefix;
    }

    @JsonProperty
    public String getEquivalentClass() {
        return equivalentClass;
    }

    @JsonProperty
    public void setEquivalentClass(String equivalentClass) {
        this.equivalentClass = equivalentClass;
    }
}
