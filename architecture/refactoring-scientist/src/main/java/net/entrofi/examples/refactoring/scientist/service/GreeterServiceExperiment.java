package net.entrofi.examples.refactoring.scientist.service;

import com.github.rawls238.scientist4j.Experiment;
import com.github.rawls238.scientist4j.Result;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;

public class GreeterServiceExperiment extends Experiment<String> {

    private volatile Result<String> result;


    @Override
    protected void publish(Result<String> result) {
        this.result = result;
        MetricRegistry metricRegistry = this.getMetrics(null);
        MetricName greetingGauge = MetricName.build(GreeterServiceExperiment.class.getCanonicalName(), "greeting");

        if (metricRegistry.getMetrics().get(greetingGauge) == null) {
            Gauge<String> gauge = this::getResultGaugeValue;
            metricRegistry.register(MetricRegistry.name(greetingGauge.getKey()), gauge);
        }

    }

    private String getResultGaugeValue() {
        if (getResult() != null && getResult().getCandidate().isPresent()) {
            if (Boolean.FALSE.equals(getResult().getMatch().get())) {
                return getResult().getCandidate().get().getValue() + " does  not match " + getResult().getControl().getValue();
            } else {
                return getResult().getCandidate().get().getValue() + " matches " + getResult().getControl().getValue();
            }
        }
        return "Nothing to say!";
    }


    private Result<String> getResult() {
        return this.result;
    }
}
