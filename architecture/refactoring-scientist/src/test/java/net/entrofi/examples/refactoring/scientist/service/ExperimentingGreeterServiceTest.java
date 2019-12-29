package net.entrofi.examples.refactoring.scientist.service;

import io.dropwizard.metrics5.ConsoleReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ExperimentingGreeterServiceTest {


    private ConsoleReporter reporter;
    private ExperimentingGreeterService service;

    @Before
    public void setup() {
        service = new ExperimentingGreeterService();
        reporter = ConsoleReporter
                .forRegistry(
                        service.getServiceExperiment().getMetrics(null)
                )
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
    }

    @Test
    public void greet() {

        for( int i = 0; i < 10; i++) {
            service.greet("Comak");
        }
        reporter.report();
    }
}