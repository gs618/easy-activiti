package com.github.gs618.easy.activiti.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author s.c.gao
 */
@Data
public class ProcessDefinitionDTO implements Serializable {

    String id;

    String name;

    String key;

    String tenantId;

}
