package main;

import com.github.drapostolos.rdp4j.*;
import com.google.gson.Gson;
import com.hierynomus.smbj.io.InputStreamByteChunkProvider;
import com.hierynomus.smbj.share.File;
import eu.nites.esb.iec.common.message.base.IecMessage;
import eu.nites.esb.iec.common.message.base.Payload;
import eu.nites.esb.iec.common.message.meterreadings.MeterReadings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FileListener implements DirectoryListener, IoErrorListener, InitialContentListener {

    private static final Log LOG = LogFactory.getLog(FileListener.class);

    private final FtpDirectory directory;
    private final List<String> waiting = new LinkedList<>();

    public FileListener(FtpDirectory directory) {
        this.directory = directory;
    }

    @Override
    public void fileAdded(FileAddedEvent event) {
        boolean isDir = event.getFileElement().isDirectory();
        LOG.info("Added: " + (isDir ? "DIR--" : "FILE-") + event.getFileElement()
                + (isDir ? " - Since the file is a DIRECTORY it will NOT be processed."
                : ""));

        String extension = "";
        int i = event.getFileElement().toString().lastIndexOf('.');
        if (i > 0) {
            extension = event.getFileElement().toString().substring(i + 1);
        }

        if (!isDir && extension.equals("txt") && !waiting.contains(event.getFileElement().toString())) {
            waiting.add(event.getFileElement().toString());
        }

    }

    @Override
    public void fileRemoved(FileRemovedEvent event) {
        waiting.remove(event.getFileElement().getName());
    }

    @Override
    public void fileModified(FileModifiedEvent event) {
    }

    @Override
    public void ioErrorCeased(IoErrorCeasedEvent event) {
    }

    @Override
    public void ioErrorRaised(IoErrorRaisedEvent event) {
    }

    @Override
    public void initialContent(InitialContentEvent event) {
    }

    public void processQueued() {
        while (!waiting.isEmpty()){
            parseFile(waiting.remove(0));
        }
    }

    private void parseFile(String fileName) {
        try {
            String dirPath = directory.getDirPath() + "/" + fileName;
            File file = directory.openFile(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

            IecMessage iecMessage = new IecMessage();
            Transformer.setIecHeader(iecMessage, dirPath);

            MeterReadings mrs = new MeterReadings();
            mrs.setMeterReading(new ArrayList<>());
            Payload payload = new Payload();
            payload.setBasePayload(mrs);
            iecMessage.setPayload(payload);

            String line;
            while ((line = reader.readLine()) != null) {
                Transformer.convertIec(iecMessage, line, dirPath);
            }

            //worker set
            //Transformer.sendIecToServer(iecMessage, dirPath);
            System.out.println(new Gson().toJson(iecMessage));  //after you check that console print is fine, send to server

            reader.close();
            file.close();
            archiveFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void archiveFile(String fileName) {
        try {
            File fromFile = directory.openFile(fileName);
            File toFile = directory.openArchiveFile(new Date().getTime() + "_" + fileName);

            toFile.write(new InputStreamByteChunkProvider(new ByteArrayInputStream(IOUtils.toByteArray(fromFile.getInputStream()))));
            toFile.close();

            fromFile.deleteOnClose();
            fromFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}