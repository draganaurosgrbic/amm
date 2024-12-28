package main;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.protocol.commons.EnumWithValue;

public class FtpFile implements FileElement {

    private final FileIdBothDirectoryInformation file;

    public FtpFile(FileIdBothDirectoryInformation file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getFileName();
    }

    @Override
    public boolean isDirectory() {
        return EnumWithValue.EnumUtils.isSet(file.getFileAttributes(), FileAttributes.FILE_ATTRIBUTE_DIRECTORY);
    }

    @Override
    public long lastModified() {
        return file.getLastWriteTime().getWindowsTimeStamp();
    }

    @Override
    public String toString() {
        return file.getFileName();
    }

}