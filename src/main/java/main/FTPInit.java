package main;

import com.github.drapostolos.rdp4j.DirectoryPoller;
import config.Configuration;
import config.InitSapConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class FTPInit {

    private static final Log LOG = LogFactory.getLog(FTPInit.class);
    private static final ArrayList<DirectoryPoller> directoryPollers = new ArrayList<>();

    private static Timer timer = null;
    private static TimerTask hourlyTask = null;

    private static Timer configTimer = null;
    private static TimerTask configTimerTask = null;


    @PostConstruct
    public void configInit() {

        LOG.info("POST CONSTRUCT AMM");

        try {
            configTimer = new Timer();
            configTimerTask = new TimerTask() {
                @Override
                public void run() {
                    LOG.info("Config reloading");
                    remove();
                    init();
                    LOG.info("Config reloaded");
                }
            };
            int reloadTime = 480;
            try {
                reloadTime = Integer.parseInt(Configuration.configReloadTime);
            } catch (Exception ignored) {
            }
            LOG.info("Schedule start");
            configTimer.schedule(configTimerTask, 1000 * 60L, 1000 * 60L * reloadTime);
            LOG.info("Schedule end");
        } catch (Exception e) {
            LOG.fatal("Failed to reload config");
        }
    }


    public void init() {

        LOG.info("AMM Adapter config initializing!");
        InitSapConfiguration.init();
        directoryPollers.clear();
        LOG.info("AMM Adapter initializing!");

        try {

            String host = Configuration.smbHost; //"smb://odsdevisapp.eps.local/NITES/AMM/";
            String share = Configuration.smbShare;
            String archiveDir = Configuration.smbArchiveDir;//"smb://odsdevisapp.eps.local/NITES/ARCH/AMM/";

            String username = Configuration.smbUsername;
            String password = Configuration.smbPassword;
            String domain = Configuration.smbDomain;

            FtpDirectory.openConnection(host, username, password, domain, share, archiveDir);

            ArrayList<FileListener> listeners = new ArrayList<>();

            for (String dirPath : Configuration.smbDirs) {
                LOG.info("ADDING DIR: " + dirPath);

                FtpDirectory polledDirectory = new FtpDirectory(dirPath);
                FileListener listener = new FileListener(polledDirectory);
                listeners.add(listener);

                directoryPollers.add(DirectoryPoller.newBuilder().addPolledDirectory(polledDirectory).addListener(listener)
                        .enableFileAddedEventsForInitialContent().setPollingInterval(10, TimeUnit.SECONDS).start());
            }

            LOG.info("DirectoryPoller is set");
            timer = new Timer();
            hourlyTask = new TimerTask() {
                @Override
                public void run() {
                    for (FileListener listener : listeners) {
                        listener.processQueued();
                    }
                }
            };

            int time = 300;
            try {
                time = Integer.parseInt(Configuration.smbCheckIntervalSec);
            } catch (Exception ignored) {
            }
            timer.schedule(hourlyTask, 1000 * 60L, 1000L * time);
            LOG.info("AMM Adapter initializing SUCCESSFUL!");
        } catch (Exception e) {
            for (DirectoryPoller dp : directoryPollers) {
                dp.stop();
            }
            timer.cancel();
            timer.purge();
            hourlyTask.cancel();
            LOG.fatal("AMM Adapter initializing ERROR!" + e.toString());
        }

    }

    @PreDestroy
    public void remove() {
        LOG.info("AMM Adapter ENDED");
        if (timer != null) {
            timer.cancel();
            timer.purge();
            hourlyTask.cancel();
        }
        for (DirectoryPoller dp : directoryPollers) {
            dp.stop();
            dp.stopNow();
        }
        FtpDirectory.closeConnection();
    }

}