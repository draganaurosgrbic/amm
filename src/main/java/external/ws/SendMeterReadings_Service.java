package external.ws;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 */
@WebServiceClient(name = "SendMeterReadings", targetNamespace = "http://meterreadings.ws.esb.nites.eu/", wsdlLocation = "http://10.100.2.81:8080/SAPDuMeterReadings/SendMeterReadings?wsdl")
public class SendMeterReadings_Service
        extends Service {

    private final static URL SENDMETERREADINGS_WSDL_LOCATION;
    private final static WebServiceException SENDMETERREADINGS_EXCEPTION;
    private final static QName SENDMETERREADINGS_QNAME = new QName("http://meterreadings.ws.esb.nites.eu/", "SendMeterReadings");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://10.100.2.81:8080/SAPDuMeterReadings/SendMeterReadings?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        SENDMETERREADINGS_WSDL_LOCATION = url;
        SENDMETERREADINGS_EXCEPTION = e;
    }

    public SendMeterReadings_Service() {
        super(__getWsdlLocation(), SENDMETERREADINGS_QNAME);
    }

    public SendMeterReadings_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), SENDMETERREADINGS_QNAME, features);
    }

    public SendMeterReadings_Service(URL wsdlLocation) {
        super(wsdlLocation, SENDMETERREADINGS_QNAME);
    }

    public SendMeterReadings_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SENDMETERREADINGS_QNAME, features);
    }

    public SendMeterReadings_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SendMeterReadings_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * @return returns SendMeterReadings
     */
    @WebEndpoint(name = "SendMeterReadingsPort")
    public SendMeterReadings getSendMeterReadingsPort() {
        return super.getPort(new QName("http://meterreadings.ws.esb.nites.eu/", "SendMeterReadingsPort"), SendMeterReadings.class);
    }

    /**
     * @param features A list of {@link WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns SendMeterReadings
     */
    @WebEndpoint(name = "SendMeterReadingsPort")
    public SendMeterReadings getSendMeterReadingsPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://meterreadings.ws.esb.nites.eu/", "SendMeterReadingsPort"), SendMeterReadings.class, features);
    }

    private static URL __getWsdlLocation() {
        if (SENDMETERREADINGS_EXCEPTION != null) {
            throw SENDMETERREADINGS_EXCEPTION;
        }
        return SENDMETERREADINGS_WSDL_LOCATION;
    }

}
