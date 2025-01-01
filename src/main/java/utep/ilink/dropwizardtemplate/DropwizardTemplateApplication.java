package utep.ilink.dropwizardtemplate;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import utep.ilink.dropwizardtemplate.config.MainConfig;
import utep.ilink.dropwizardtemplate.health.TemplateHealthCheck;
import utep.ilink.dropwizardtemplate.resources.MainResource;
import io.federecio.dropwizard.swagger.*;

public class DropwizardTemplateApplication extends Application<MainConfig> {

  public static void main(String[] args) throws Exception {
    new DropwizardTemplateApplication().run(args);
  }

  @Override
  public String getName() {
    return "hello-world";
  }

  @Override
  public void initialize(Bootstrap<MainConfig> bootstrap) {
    bootstrap.addBundle(
      new SwaggerBundle<MainConfig>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          MainConfig configuration
        ) {
          return configuration.swaggerBundleConfiguration;
        }
      }
    );
  }

  @Override
  public void run(MainConfig configuration, Environment environment) {
    final MainResource resource = new MainResource(
      configuration.getOntologySource(),
      configuration.getOntologySavePath(),
      configuration.getIriPrefix(),
      configuration.getEquivalentClass()
    );
    final TemplateHealthCheck healthCheck = new TemplateHealthCheck(
      configuration.getTemplate()
    );
    environment.healthChecks().register("template", healthCheck);
    environment.jersey().register(resource);
  }
}
