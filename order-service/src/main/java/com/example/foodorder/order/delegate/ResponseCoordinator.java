package com.example.foodorder.order.delegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ResponseCoordinator acts as a shared registry of CountDownLatches keyed by
 * correlation IDs. When a delegate sends an ActiveMQ request, it registers a latch
 * and blocks on await(). When the response listener receives the reply, it calls
 * resolve() to unblock the waiting delegate thread.
 */
@Component
public class ResponseCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ResponseCoordinator.class);

    private final Map<String, CountDownLatch> latches = new ConcurrentHashMap<>();
    private final Map<String, String> results = new ConcurrentHashMap<>();

    /**
     * Register a new latch for the given correlationId.
     */
    public void register(String correlationId) {
        latches.put(correlationId, new CountDownLatch(1));
    }

    /**
     * Block the calling thread until the response is resolved or timeout (in seconds).
     * @return the resolved result string, or null on timeout
     */
    public String await(String correlationId, int timeoutSeconds) throws InterruptedException {
        CountDownLatch latch = latches.get(correlationId);
        if (latch == null) {
            throw new IllegalStateException("No latch registered for: " + correlationId);
        }
        latch.await(timeoutSeconds, TimeUnit.SECONDS);
        latches.remove(correlationId);
        return results.remove(correlationId);
    }

    /**
     * Called by the response listener to unblock a waiting delegate.
     * @param correlationId the matching correlationId from the request
     * @param result the response payload (e.g. "SUCCESS", "FAILED", or driver name)
     */
    public void resolve(String correlationId, String result) {
        CountDownLatch latch = latches.get(correlationId);
        if (latch != null) {
            results.put(correlationId, result);
            latch.countDown();
        } else {
            log.warn("No latch found for correlationId={}", correlationId);
        }
    }
}
