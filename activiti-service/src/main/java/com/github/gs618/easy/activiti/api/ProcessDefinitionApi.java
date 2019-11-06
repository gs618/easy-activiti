package com.github.gs618.easy.activiti.api;

import com.github.gs618.easy.activiti.config.UploadFileProperties;
import com.github.gs618.easy.activiti.dto.ProcessDefinitionDTO;
import com.github.gs618.easy.activiti.exception.FileReadException;
import com.github.gs618.easy.activiti.service.UploadFileService;
import com.github.gs618.easy.activiti.util.ProcessDefinitionConverter;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author s.c.gao
 */
@RestController
@RequestMapping("/")
@Slf4j
public class ProcessDefinitionApi {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private UploadFileProperties uploadFileProperties;

    @GetMapping("/process-definitions")
    public List<ProcessDefinitionDTO> list() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionName()
                .orderByProcessDefinitionVersion().desc()
                .list();
        List<ProcessDefinitionDTO> processDefinitionDTOs = new ArrayList<>(processDefinitions.size());
        processDefinitions.forEach(processDefinition -> {
            ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
            BeanUtils.copyProperties(processDefinition, processDefinitionDTO);
            processDefinitionDTOs.add(processDefinitionDTO);
        });
        return processDefinitionDTOs;
    }

    @GetMapping("/process-definition/{id}")
    public ProcessDefinitionDTO get(@PathVariable("id") String id) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(id);
        return ProcessDefinitionConverter.toDTO(processDefinition);
    }

    @PostMapping("/process-definition/{tenant-id}")
    public String deploy(@RequestParam("file") MultipartFile file
            , @PathVariable("tenant-id") String tenantId) {
        String relativePath = uploadFileService.upload(file, null, true);
        String originalFilename = file.getOriginalFilename();
        String processName = originalFilename.substring(0, originalFilename.indexOf('.'));
        Deployment deployment;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(relativePath)) {
            deployment = repositoryService.createDeployment()
                    .name(processName)
                    .tenantId(tenantId)
                    .addInputStream(originalFilename, inputStream).deploy();
        } catch (IOException e) {
            log.error("Deploy file [" + relativePath + "] in error");
            throw new FileReadException();
        }
        try {
            Files.deleteIfExists(Paths.get(uploadFileProperties.getBasePath(), File.separator, relativePath));
        } catch (IOException e) {
            log.error("Delete file [" + relativePath + "] in error");
            throw new FileReadException();
        }
        return deployment.getId();
    }
}
