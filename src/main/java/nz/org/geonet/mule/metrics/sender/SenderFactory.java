package nz.org.geonet.mule.metrics.sender;

import nz.org.geonet.mule.exception.SenderException;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Creates Senders.
 *
 * @author Geoff Clitheroe
 *         Date: 8/16/13
 *         Time: 1:49 PM
 */
public class SenderFactory {

    private final static Logger log = Logger.getLogger(SenderFactory.class.getSimpleName());

    /**
     * Creates Sender configured based on properties.
     *
     * @return a Sender
     * @throws SenderException if can't find enough properties to configure a Sender.
     */
    public static Sender getSender(Properties properties) throws SenderException {

        Sender sender;

        if (properties.getProperty("librato.user") != null && properties.getProperty("librato.api.key") != null) {
            sender = new LibratoMetricsSender(properties.getProperty("librato.user"), properties.getProperty("librato.api.key"));
            log.info("creating a Librato sender");
        } else if (properties.getProperty("hostedgraphite.api.key") != null) {
            sender = new HostedGraphiteSender(properties.getProperty("hostedgraphite.api.key"));
            log.info("creating a Hosted Graphite Sender");
        } else {
            sender = new LogSender();
            log.info("creating a log sender");
        }

        return sender;
    }
}
