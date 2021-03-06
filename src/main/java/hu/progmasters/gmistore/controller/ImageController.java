package hu.progmasters.gmistore.controller;

import hu.progmasters.gmistore.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public @ResponseBody
    String[] uploadPicture(@RequestParam("picture") MultipartFile imageToUpload) throws IOException {
        return imageService.uploadImage(imageToUpload);
    }
}
