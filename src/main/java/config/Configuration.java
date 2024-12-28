package config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Configuration {
    private static final Log LOG = LogFactory.getLog(Configuration.class);

    public static String smbHost = "";
    public static String smbShare = "";
    public static String smbArchiveDir = "";

    public static String smbUsername = "";
    public static String smbPassword = "";
    public static String smbDomain = "";

    public static ArrayList<String> smbDirs = new ArrayList<>();

    public static String smbCheckIntervalSec = "";
    public static String configReloadTime = "";

    public static String eps_base_url = "";
    public static String eps_amm = "";

    public static void initializeSmb() {
        readConfigurationFileSmb(Configuration.class.getResource(ConfigurationConstants.ADAPTER_CONFIGURATION_FILENAME).getPath());
        readConfigurationFileWSDL(Configuration.class.getResource(ConfigurationConstants.ADAPTER_CONFIGURATION_FILENAME).getPath());
    }

    private static void readConfigurationFileSmb(String path) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            LOG.fatal("Properties file not found. " + e);
        } catch (IOException e) {
            LOG.fatal("Error opening the file. " + e);
        }

        smbHost = properties.getProperty(ConfigurationConstants.SMB_HOST);
        smbShare = properties.getProperty(ConfigurationConstants.SMB_SHARE);
        smbArchiveDir = properties.getProperty(ConfigurationConstants.SMB_ARCHIVE_DIR);

        smbUsername = properties.getProperty(ConfigurationConstants.SMB_USERNAME);
        smbPassword = properties.getProperty(ConfigurationConstants.SMB_PASSWORD);

        smbCheckIntervalSec = properties.getProperty(ConfigurationConstants.SMB_CHECK_INTERVAL_SEC);
        configReloadTime = properties.getProperty(ConfigurationConstants.CONFIG_RELOAD_TIME);

        int dirsSize = properties.size() - 9;
        if (dirsSize > 0) {
            for (int i = 1; i <= dirsSize; ++i) {
                if (properties.containsKey("SMB_USER" + i)) {
                    smbDirs.add(properties.getProperty("SMB_USER" + i));
                    LOG.info("SMB IN: " + properties.getProperty("SMB_USER" + i));
                }
            }
        }

    }

    private static void readConfigurationFileWSDL(String path) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            LOG.fatal("Properties file not found. " + e);
        } catch (IOException e) {
            LOG.fatal("Error opening the file. " + e);
        }

        eps_base_url = properties.getProperty(ConfigurationConstants.EPS_BASE_URL);
        eps_amm = properties.getProperty(ConfigurationConstants.EPS_AMM);
    }

}
