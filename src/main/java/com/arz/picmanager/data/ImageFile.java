package com.arz.picmanager.data;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFile {

    private File file;
    private long size;
    private String md5;

    public ImageFile(File file) {
        this.file = file;
        this.size = file.length();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ImageFile{" +
                "file=" + file +
                ", size=" + size +
                '}';
    }

    private void setMd5() throws IOException {
        InputStream is = new FileInputStream(this.file);
        this.md5 = DigestUtils.md5Hex(is);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageFile other = (ImageFile) o;

        if (other.size != this.size) {
            return false;
        } else {
            try {
                if (this.md5 == null) {
                    this.setMd5();
                }
                if (other.md5 == null) {
                    other.setMd5();
                }
                if (this.md5.equals(other.md5)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
