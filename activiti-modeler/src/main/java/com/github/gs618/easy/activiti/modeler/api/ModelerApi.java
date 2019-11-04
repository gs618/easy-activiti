package com.github.gs618.easy.activiti.modeler.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.gs618.easy.activiti.dto.ModelDTO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author s.c.gao
 */
@RestController
@Slf4j
@RequestMapping("/model")
public class ModelerApi {

    public static final int REVISION = 1;
    @Autowired
    RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建模型
     */
    @PostMapping
    public Model newModel(@RequestBody ModelDTO modelDTO) {
        //初始化一个空模型
        Model model = repositoryService.newModel();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelDTO.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, modelDTO.getDescription());
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, REVISION);
        model.setMetaInfo(modelNode.toString());

        model.setName(modelDTO.getName());
        model.setKey(modelDTO.getKey());
        model.setTenantId(modelDTO.getTenantId());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "gs618");
        editorNode.put("resourceId", "gs618");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id, editorNode.toString().getBytes(StandardCharsets.UTF_8));
        return model;
    }

    /**
     * 获取所有模型
     *
     * @return
     */
    @GetMapping
    public List<Model> modelList() {
        return repositoryService.createModelQuery().orderByCreateTime().desc().list();
    }

    /**
     * 删除模型
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
    }

    private byte[] modelToBpmnModel(byte[] bytes) throws IOException {
        if (ObjectUtils.isEmpty(bytes)) {
            log.error("Model is empty");
            return null;
        }
        JsonNode modelNode = new ObjectMapper().readTree(bytes);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (bpmnModel.getProcesses().isEmpty()) {
            log.error("One process is required at least");
            return null;
        }
        return new BpmnXMLConverter().convertToXML(bpmnModel);
    }

    private void deploy(Model model) throws Exception {
        byte[] bpmnBytes = modelToBpmnModel(repositoryService.getModelEditorSource(model.getId()));
        if (Objects.isNull(bpmnBytes)) {
            return;
        }
        //发布流程
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .tenantId(model.getTenantId())
                .addString(model.getName() + "_" + model.getVersion()
                        , new String(bpmnBytes, StandardCharsets.UTF_8))
                .deploy();
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);
    }

    /**
     * 发布模型为流程定义
     *
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("/deploy/{id}")
    public void deploySpecialId(@PathVariable("id") String id) throws Exception {
        Model model = getModelById(id);
        if (Objects.isNull(model)) {
            log.error("no model with id " + id);
            return;
        }
        deploy(model);
    }

    private Model getModelById(String id) {
        if (StringUtils.isBlank(id)) {
            log.info("Model id is empty");
            return null;
        }
        return repositoryService.getModel(id);
    }

    /**
     * 导出模型
     *
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<ByteArrayResource> export(@PathVariable("id") String id) throws Exception {
        Model model = getModelById(id);
        if (Objects.isNull(model)) {
            log.error("no model with id " + id);
            return null;
        }
        byte[] bpmnBytes = modelToBpmnModel(repositoryService.getModelEditorSource(model.getId()));
        if (Objects.isNull(bpmnBytes)) {
            return null;
        }
        String filename = model.getName() + ".bpmn20.xml";
        MediaType mediaType = MediaType.APPLICATION_XML;
        ByteArrayResource resource = new ByteArrayResource(bpmnBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(mediaType)
                .contentLength(bpmnBytes.length)
                .body(resource);
    }

}
