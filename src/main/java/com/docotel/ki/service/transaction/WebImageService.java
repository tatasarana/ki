package com.docotel.ki.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

@Service
public class WebImageService {
    @Value("${upload.file.web.image:}")
    private String pathImage;

    public String setImage(String nameFileImage) {
        /*Load Image From Web-Image*/
        String image = "";
        try {
            File file = new File(pathImage + nameFileImage);
            if (file.exists() && !file.isDirectory()) {
                FileInputStream fileInputStreamReader = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fileInputStreamReader.read(bytes);
                image = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(bytes);
            } else {
                image = "img/" + nameFileImage;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }
}
