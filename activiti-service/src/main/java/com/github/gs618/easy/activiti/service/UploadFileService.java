package com.github.gs618.easy.activiti.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author s.c.gao
 */
public interface UploadFileService {

    /**
     * 上传文件
     *
     * @param file
     * @param customPath
     * @param keepName
     * @return
     */
    String upload(MultipartFile file, String customPath, boolean keepName);

}
