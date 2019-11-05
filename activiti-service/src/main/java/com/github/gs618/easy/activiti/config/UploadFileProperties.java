package com.github.gs618.easy.activiti.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


/**
 * @author s.c.gao
 */
@Data
@ConfigurationProperties(prefix = UploadFileProperties.PREFIX)
@Component
public class UploadFileProperties {

    public static final String PREFIX = "easy.upload-file";

    private static final String DEFAULT_BASE_PATH = "/mnt/files";

    private String basePath = DEFAULT_BASE_PATH;

    private int limitMb = 20;

    private List<String> allowExtensionNames = Arrays.asList(
            ".xml"
    );

}
