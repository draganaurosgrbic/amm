package config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PreDestroy;

public class InitSapConfiguration {

    private static final Log LOG = LogFactory.getLog(InitSapConfiguration.class);

    public static void init() {
        LOG.info("SAP Receiver STARTED");
        try {
            LOG.info("Loading configurations!");
            Configuration.initializeSmb();
        } catch (Exception e) {
            LOG.info("Init failed");
        }
    }

    @PreDestroy
    public void remove() {
        LOG.info("SAP Receiver ENDED");
    }

}
