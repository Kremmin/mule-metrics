package nz.org.geonet.mule.metrics.sender;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Geoff Clitheroe
 *         Date: 8/26/13
 *         Time: 9:11 AM
 */
public class LogSender implements Sender {

    private final static Logger log = Logger.getLogger(LogSender.class.getSimpleName());


    String source;

    LogSender() {
        source = Util.source();
    }

    public void send(String serverType, Map<String, Number> metrics) {
        for (String key : metrics.keySet()) {
            log.info(source + "." + serverType + "." + key + " " + metrics.get(key));
        }
    }
}
