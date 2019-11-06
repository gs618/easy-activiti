package com.github.gs618.easy.activiti.dto;

import lombok.Data;

/**
 * @author s.c.gao
 */
@Data
public class TaskDefinitionDTO {

    String key;

    String name;

    Integer priority;

    String assignee;
}
