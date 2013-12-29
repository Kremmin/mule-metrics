package nz.org.geonet.mule.metrics.sender;

import java.util.Map;

/**
 * Sender interface
 *
 * @author Geoff Clitheroe
 * Date: 8/16/13
 * Time: 1:48 PM
 */
public interface Sender {

    /**
     * Send metrics to the collector
     * @param serverType the appServer type e.g., Mule.
     * @param metrics the metrics to send.
     */
    void send(String serverType, Map<String, Number> metrics);
}
