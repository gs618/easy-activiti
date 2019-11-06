package com.github.gs618.easy.activiti.service.impl;

import com.github.gs618.easy.activiti.config.UploadFileProperties;
import com.github.gs618.easy.activiti.exception.FileReadException;
import com.github.gs618.easy.activiti.service.UploadFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author s.c.gao
 */
@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {

    public static final long BYTE_C = 1024L;

    private static final String DEFAULT_PATH = "processes";

    private static final String EXTENSION_SEPERATOR = ".";
    public static final int RANDOM_NAME_LENGTH = 10;

    @Autowired
    private UploadFileProperties uploadFileProperties;

    @Override
    public String upload(MultipartFile file, String customPath, boolean keepName) {
        String originalFilename = file.getOriginalFilename();
        // Size check
        long fileSize = file.getSize();
        if (fileSize > uploadFileProperties.getLimitMb() * BYTE_C) {
            log.info("file size is bigger than " + uploadFileProperties.getLimitMb() + " MB");
            return null;
        }
        // extention check
        boolean allowedEx = false;
        for (String allowExtensionName : uploadFileProperties.getAllowExtensionNames()) {
            if (originalFilename.endsWith(allowExtensionName)) {
                allowedEx = true;
                break;
            }
        }
        if (!allowedEx) {
            log.info("Extension Name of file [" + originalFilename + "] is not allowed");
            return null;
        }

        StringBuilder relativePath = new StringBuilder(100);
        if (StringUtils.isNotBlank(customPath)) {
            relativePath.append(customPath);
        } else {
            relativePath.append(DEFAULT_PATH);
        }
        relativePath.append(File.separator);
        // 创建目录
        String parentPath = uploadFileProperties.getBasePath()
                + File.separator
                + relativePath.toString();
        mkParentDir(parentPath);

        if (!keepName) {
            int lastIndexOf = originalFilename.lastIndexOf(EXTENSION_SEPERATOR);
            String extName = originalFilename.substring(lastIndexOf >= 0 ? lastIndexOf : originalFilename.length());
            relativePath.append(RandomStringUtils.randomAlphanumeric(RANDOM_NAME_LENGTH));
            relativePath.append(extName);
        } else {
            relativePath.append(originalFilename);
        }
        Path path = Paths.get(uploadFileProperties.getBasePath()
                + File.separator
                + relativePath.toString());
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileReadException();
        }
        return relativePath.toString();
    }

    /**
     * 创建父路径
     *
     * @param parentDir
     */
    private void mkParentDir(String parentDir) {
        // 创建目录
        Path parentPath = Paths.get(parentDir);
        File parentFile = parentPath.toFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
    }
}
