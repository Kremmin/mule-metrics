package nz.org.geonet.mule;

import nz.org.geonet.mule.metrics.collector.MuleCollector;
import nz.org.geonet.mule.metrics.sender.Sender;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Geoff Clitheroe
 *         Date: 12/25/13
 *         Time: 2:34 PM
 */
public class Metrics {

    MuleCollector collector = null;
    Sender sender = null;

    public void setCollector(MuleCollector collector) {
        this.collector = collector;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void monitor() {
        sender.send("Mule", collector.gather());
    }
}
