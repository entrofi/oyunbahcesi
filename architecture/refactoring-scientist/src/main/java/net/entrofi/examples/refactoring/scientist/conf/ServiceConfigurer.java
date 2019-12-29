package net.entrofi.examples.refactoring.scientist.conf;

import net.entrofi.examples.refactoring.scientist.service.ExperimentingGreeterService;
import net.entrofi.examples.refactoring.scientist.service.GreeterService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ServiceConfigurer {


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GreeterService greeterService() {
        ExperimentingGreeterService greeterService = new ExperimentingGreeterService();
        return greeterService;
    }
}
