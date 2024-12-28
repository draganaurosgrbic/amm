package main;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class FtpDirectory implements PolledDirectory {

    private static DiskShare connection;
    private static String archiveDir;
    private final String dirPath;

    public static void openConnection(String host, String username, String password, String domain, String share, String archivePath) {
        try {
            connection = (DiskShare) new SMBClient().connect(host).authenticate(new AuthenticationContext(username, password.toCharArray(), domain)).connectShare(share);
            archiveDir = archivePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FtpDirectory(String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public Set<FileElement> listFiles() {
        Set<FileElement> result = new LinkedHashSet<>();

        try {
            for (FileIdBothDirectoryInformation file : connection.list(dirPath)) {
                if (".".equals(file.getFileName()) || "..".equals(file.getFileName())) {
                    //it gets some files named "." and "..", lets skip them
                    continue;
                }
                result.add(new FtpFile(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public File openFile(String fileName) {
        // I need delete mask for moving to archive file
        return connection.openFile(dirPath + "/" + fileName,
                EnumSet.of(AccessMask.GENERIC_READ, AccessMask.DELETE),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null);
    }

    public File openArchiveFile(String fileName) {
        return connection.openFile(archiveDir + "/" + fileName,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                null);
    }

    public String getDirPath() {
        return dirPath;
    }

}