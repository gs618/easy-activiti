package com.github.gs618.easy.activiti.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.gs618.easy.activiti.dto.ProcessDefinitionDTO;
import com.github.gs618.easy.activiti.service.UploadFileService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author s.c.gao
 */
@RestController
@RequestMapping("/process-definition")
public class ProcessDefinitionApi {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired

    private UploadFileService uploadFileService;

    @GetMapping
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

    @PostMapping("/{tenant-id}")
    public String deploy(@RequestParam("file") MultipartFile file
            , @PathVariable("tenant-id") String tenantId) {
        String relativePath = uploadFileService.upload(file, null, true);
        String processName = file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf('.'));
        Deployment deployment = repositoryService.createDeployment()
                .name(processName)
                .tenantId(tenantId)
                .addClasspathResource(relativePath).deploy();

        return deployment.getId();
    }
}
