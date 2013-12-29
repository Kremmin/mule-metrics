package nz.org.geonet.mule.exception;

/**
 * Connection Exception
 *
 * @author Geoff Clitheroe
 * Date: 8/16/13
 * Time: 2:21 PM
 */
public class ConnectionException extends Exception {

    public ConnectionException(String message) {
        super(message);
    }
}
