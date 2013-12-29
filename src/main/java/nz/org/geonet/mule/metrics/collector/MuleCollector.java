package nz.org.geonet.mule.metrics.collector;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Collects Mule metrics.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 12:30 PM
 */
public class MuleCollector {

    private final MetricsClient metricsClient;
    private final boolean perAppMetrics;

    private final static Logger log = Logger.getLogger(MetricsClient.class.getSimpleName());

    // Trying to reset the Mule stats counters (e.g, with an exec via JMX) results in
    // divide by zero errors.  See https://www.mulesoft.org/jira/browse/MULE-6417?focusedCommentId=72319#comment-72319
    // Keep a track of the number of things per sample interval ourselves.
    private HashMap<String, Number> metricsStore = new HashMap<String, Number>();
    List<String> metricsNames = Arrays.asList("AsyncEventsReceived", "SyncEventsReceived", "TotalProcessingTime", "TotalEventsReceived", "ProcessedEvents", "ExecutionErrors", "FatalErrors");

    // Due to problems with Mule stats these stats are not very meaningful - they will be calculated since Mule started.
    // MinProcessingTime MaxProcessingTime AverageProcessingTime


    public MuleCollector(MetricsClient metricsClient, boolean perAppMetrics) {
        this.metricsClient = metricsClient;
        this.perAppMetrics = perAppMetrics;
    }

    public HashMap<String, Number> gather() {
        HashMap<String, Number> metrics = jvmHeapMemoryUsage();
        metrics.putAll(cpuLoad());
        metrics.putAll(jvmThreads());
        metrics.putAll(classLoading());
        metrics.putAll(muleApplications());

        return metrics;
    }

    HashMap<String, Number> cpuLoad() {
        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=OperatingSystem", "ProcessCpuLoad,SystemCpuLoad");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("ProcessCpuLoad")) {
            result.put("ProcessCpuLoad", responseMap.get("ProcessCpuLoad"));
        }

        if (responseMap.containsKey("SystemCpuLoad")) {
            result.put("SystemCpuLoad", responseMap.get("SystemCpuLoad"));
        }

        return result;
    }

    HashMap<String, Number> jvmHeapMemoryUsage() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=Memory", "HeapMemoryUsage");

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("used")) {
            result.put("HeapMemoryUsage.used", responseMap.get("used"));
        }

        if (responseMap.containsKey("max")) {
            result.put("HeapMemoryUsage.max", responseMap.get("max"));
        }

        return result;
    }

    HashMap<String, Number> jvmThreads() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=Threading", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("ThreadCount")) {
            result.put("Threading.used", responseMap.get("ThreadCount"));
        }

        if (responseMap.containsKey("DaemonThreadCount")) {
            result.put("Threading.max", responseMap.get("DaemonThreadCount"));
        }

        return result;
    }

    HashMap<String, Number> classLoading() {

        HashMap<String, Number> responseMap = metricsClient.read("java.lang:type=ClassLoading", null);

        HashMap<String, Number> result = new HashMap<String, Number>();

        if (responseMap.containsKey("TotalLoadedClassCount")) {
            result.put("Classes.totalLoaded", responseMap.get("TotalLoadedClassCount"));
        }

        return result;
    }

    HashMap<String, Number> muleApplications() {

        HashMap<String, Number> result = new HashMap<String, Number>();


        // Search for all the application totals mbeans and read their stats.
        for (String mbeanName : metricsClient.search("Mule.*:type=org.mule.Statistics,Application=\"application totals\"")) {
            // Find the Mule application name from the mbean name e.g.,
            // mule-metrics-1.0.0-SNAPSHOT
            // from
            // Mule.mule-metrics-1.0.0-SNAPSHOT:Application="application totals",type=org.mule.Statistics
            String appName = mbeanName.split(":")[0].replaceAll("^Mule\\.", "");

            HashMap<String, Number> responseMap = metricsClient.read(mbeanName, null);

            for (String metricName : metricsNames) {
                String perAppMetricName = appName + "." + metricName;

                if (responseMap.containsKey(metricName)) {
                    if (metricsStore.containsKey(perAppMetricName)) {
                        // Put the metric quantity for the app for this interval into the result.
                        long intervalValue = responseMap.get(metricName).longValue() - metricsStore.get(perAppMetricName).longValue();
                        if (perAppMetrics) result.put(perAppMetricName, intervalValue);
                        // Put a total for this metric quantity for all apps for this interval into the result.
                        Number count = result.containsKey(metricName) ? result.get(metricName) : 0;
                        result.put(metricName, count.longValue() + intervalValue);
                    }
                    metricsStore.put(perAppMetricName, responseMap.get(metricName));
                }
            }

            if (perAppMetrics) {
                // Calculate a per app event processing time for the interval.
                if (result.containsKey(appName + "." + "TotalProcessingTime") && result.containsKey(appName + "." + "ProcessedEvents")) {
                    if (result.get(appName + "." + "ProcessedEvents").longValue() > 0) {
                        result.put(appName + "." + "EventProcessingTime", (long) result.get(appName + "." + "TotalProcessingTime").longValue() / result.get(appName + "." + "ProcessedEvents").longValue());
                    } else {
                        result.put(appName + "." + "EventProcessingTime", 0);
                    }
                    result.remove(appName + "." + "TotalProcessingTime");
                }
            }
        }

        // Calculate a per event processing time for all events.
        if (result.containsKey("TotalProcessingTime") && result.containsKey("ProcessedEvents")) {
            if (result.get("ProcessedEvents").longValue() > 0) {
                result.put("EventProcessingTime", (long) result.get("TotalProcessingTime").longValue() / result.get("ProcessedEvents").longValue());
            } else {
                result.put("EventProcessingTime", 0);
            }
            result.remove("TotalProcessingTime");
        }

        return result;
    }

}
