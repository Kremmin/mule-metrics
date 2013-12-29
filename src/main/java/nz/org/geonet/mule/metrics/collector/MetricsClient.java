package nz.org.geonet.mule.metrics.collector;

import org.apache.log4j.Logger;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.*;
import org.json.simple.JSONObject;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wraps the connection to Jolokia.
 *
 * @author Geoff Clitheroe
 *         Date: 8/22/13
 *         Time: 2:34 PM
 */
public class MetricsClient {

    private final J4pClient client;
    private final static Logger log = Logger.getLogger(MetricsClient.class.getSimpleName());
    private final String URL;

    /**
     * Wraps the connection to Jolokia.
     *
     * @param URL the URL to connect to the Jolokia agent on.
     */
    @SuppressWarnings("SameParameterValue")
    public MetricsClient(String URL) {
        client = J4pClient.url(URL)
                .socketTimeout(5000)
                .build();

        this.URL = URL;
    }

    /**
     * Read attributes from an mbean.  Only includes mbean attributes that are numbers in the return.
     *
     * @param mbeanName the mbean to query
     * @param attributeName attributes of the bean to query.  Can be null.
     * @return map of bean attribute names and values.
     */
    HashMap<String, Number> read(String mbeanName, String attributeName) {

        HashMap<String, Number> responseMap = new HashMap<String, Number>();

        try {
            J4pReadRequest request;

            if (attributeName != null) {
                request = new J4pReadRequest(mbeanName, attributeName);
            } else {
                request = new J4pReadRequest(mbeanName);
            }
            J4pReadResponse response = client.execute(request);

            // Oh for type inference.
            for (ObjectName objectName : response.getObjectNames()) {
                for (String attribute : response.getAttributes(objectName)) {

                    if (response.getValue(objectName, attribute) instanceof Number) {
                        responseMap.put(attribute, (Number) response.getValue(objectName, attribute));
                    } else if (response.getValue(objectName, attribute) instanceof JSONObject) {
                        JSONObject jsonObject = response.getValue(objectName, attribute);

                        for (Object key : jsonObject.keySet()) {

                            if (jsonObject.get(key) instanceof Number) {
                                responseMap.put((String) key, (Number) jsonObject.get(key));
                            }
                        }
                    }
                }
            }

        } catch (J4pException e) {
            log.debug(e);
        } catch (MalformedObjectNameException e) {
            log.debug(e);
        }

        return responseMap;
    }

    /**
     * Execute a method on an mbean.
     *
     * @param mbeanName the name of the bean.
     * @param method the name of the method to execute.
     */
    @SuppressWarnings("SameParameterValue")
    public void exec(String mbeanName, String method) {
        try {
            J4pExecRequest request = new J4pExecRequest(mbeanName, method);
            client.execute(request);
        } catch (MalformedObjectNameException e) {
            log.debug(e);
        } catch (J4pException e) {
            log.debug(e.getStackTrace());
        }
    }

    /**
     * Search for names of mbeans matching a pattern.
     *
     * @param mbeanName
     * @return
     */
    @SuppressWarnings("SameParameterValue")
    public List<String> search(String mbeanName) {
        List<String> result = new ArrayList<String>();

        try {
            J4pSearchRequest request = new J4pSearchRequest(mbeanName);

            J4pSearchResponse response = client.execute(request);

            if (response != null && response.getMBeanNames() != null && response.getMBeanNames().size() > 0) {
                result.addAll(response.getMBeanNames());
            }

        } catch (MalformedObjectNameException e) {
            log.debug(e);
        } catch (J4pException e) {
            log.debug(e);
        }

        return result;
    }
}
