package com.waltercasis.homevision.homevisioncodetest.client;

import com.waltercasis.homevision.homevisioncodetest.model.House;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@Service
public class PhotoService {
    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    @Value("${photo.folder}")
    private String photoFolder;

    public void deletePhotosFolder() {
        File folder = new File(photoFolder);
        try {
            FileUtils.deleteDirectory(folder);
            log.info("Se ha eliminado la carpeta 'photos'");
        } catch (IOException e) {
            log.error("Error al eliminar la carpeta 'photos'", e);
        }
    }

    public void createPhotosFolder() {
        File folder = new File(photoFolder);
        if (folder.mkdir()) {
            log.info("Se ha creado una nueva carpeta 'photos'");
        } else {
            log.error("No se ha podido crear una nueva carpeta 'photos'");
        }
    }

    public void downloadPhoto(House house) {
        String url = house.getPhotoUrl();
        String extension = FilenameUtils.getExtension(url);
        String filename = house.getId() + "-" + house.getAddress() + "." + extension;
        WebClient webClient = WebClient.builder().build();
        webClient.get()
                .uri(url)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(byte[].class).doOnNext(bytes -> {
                            try {
                                FileUtils.writeByteArrayToFile(new File(photoFolder, filename), bytes);
                                log.info("Photo of house " + house.getId() + " downloaded and saved successfully");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        return Mono.error(new RuntimeException("Failed to download photo"));
                    }
                })
                .subscribe();
    }

    public void savePhoto(byte[] bytes, String filename) throws IOException {
        File photoFile = new File(photoFolder + File.separator + filename);
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            fos.write(bytes);
        }
    }
}
