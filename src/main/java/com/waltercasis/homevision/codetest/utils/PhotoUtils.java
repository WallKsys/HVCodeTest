package com.waltercasis.homevision.codetest.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class PhotoUtils {


    public PhotoUtils() {
    }

    public String getExtensionFromUrl(String photoUrl) {
        return photoUrl.substring(photoUrl.lastIndexOf(".") + 1);
    }

    public String createFileName(String id, String address, String extension) {
        return id + "-" + address + "." + extension;
    }

    public String createFilePath(String directoryPath, String fileName) {
        return directoryPath + File.separator + fileName;
    }

    public void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }



}