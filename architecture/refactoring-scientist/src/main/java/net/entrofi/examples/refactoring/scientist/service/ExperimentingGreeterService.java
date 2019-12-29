package net.entrofi.examples.refactoring.scientist.service;

import io.dropwizard.metrics5.ConsoleReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class ExperimentingGreeterService implements GreeterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentingGreeterService.class);

    private GreeterService oldService;

    private GreeterService newService;

    private GreeterServiceExperiment serviceExperiment;

    private ConsoleReporter reporter;

    public ExperimentingGreeterService() {
        this.oldService = new OldGreeterService();
        this.newService = new NewGreeterService();
        this.serviceExperiment = new GreeterServiceExperiment();
    }

    @Override
    public String greet(String name) {
        initReporter();
        Supplier<String> oldSupplier = () -> this.oldService.greet(name);
        Supplier<String> newSupplier = () -> this.newService.greet(name);
        String greetingMessage = null;
        try {
            greetingMessage = this.serviceExperiment.run(oldSupplier, newSupplier);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while running the experiment", e);
        }
        reportAndStop();
        return greetingMessage;
    }

    public GreeterServiceExperiment getServiceExperiment() {
        return serviceExperiment;
    }

    private void initReporter() {
        reporter = ConsoleReporter
                .forRegistry(
                        this.getServiceExperiment().getMetrics(null)
                )
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MILLISECONDS);
    }

    private void reportAndStop() {
        reporter.report();
        reporter.stop();
    }
}
