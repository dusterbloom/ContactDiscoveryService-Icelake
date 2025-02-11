
package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

@Context
@ConfigurationProperties("azure.retry")
public class RetryPolicyConfiguration {
    
    @Min(1)
    @Max(10)
    private int maxRetries = 3;

    @NotNull
    private Duration initialRetryInterval = Duration.ofMillis(100);

    @NotNull
    private Duration maxRetryInterval = Duration.ofSeconds(30);

    @Min(1)
    @Max(3)
    private double backoffCoefficient = 2.0;

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getInitialRetryInterval() {
        return initialRetryInterval;
    }

    public void setInitialRetryInterval(Duration initialRetryInterval) {
        this.initialRetryInterval = initialRetryInterval;
    }

    public Duration getMaxRetryInterval() {
        return maxRetryInterval;
    }

    public void setMaxRetryInterval(Duration maxRetryInterval) {
        this.maxRetryInterval = maxRetryInterval;
    }

    public double getBackoffCoefficient() {
        return backoffCoefficient;
    }

    public void setBackoffCoefficient(double backoffCoefficient) {
        this.backoffCoefficient = backoffCoefficient;
    }
}