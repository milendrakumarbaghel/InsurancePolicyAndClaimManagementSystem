package org.springboot.insurancemanagementsystem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl
        implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(
            MultipartFile file) {

        try {

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.emptyMap()
                    );

            String url =
                    result.get("secure_url").toString();

            log.info(
                    "File uploaded successfully: {}",
                    url);

            return url;

        } catch (IOException ex) {

            log.error(
                    "Cloudinary upload failed",
                    ex);

            throw new BusinessException(
                    "Unable to upload file");
        }
    }

    @Override
    public void deleteFile(
            String publicId) {

        try {

            cloudinary.uploader()
                    .destroy(
                            publicId,
                            ObjectUtils.emptyMap());

            log.info(
                    "Cloudinary file deleted: {}",
                    publicId);

        } catch (Exception ex) {

            log.error(
                    "Cloudinary delete failed",
                    ex);

            throw new BusinessException(
                    "Unable to delete file");
        }
    }
}