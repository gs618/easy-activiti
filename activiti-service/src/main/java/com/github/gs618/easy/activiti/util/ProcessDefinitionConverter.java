package com.github.gs618.easy.activiti.util;

import com.alibaba.druid.util.StringUtils;
import com.github.gs618.easy.activiti.dto.ProcessDefinitionDTO;
import com.github.gs618.easy.activiti.dto.TaskDefinitionDTO;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcessDefinitionConverter {

    public static final int FIRST_TASK_TAG = 1;

    public static ProcessDefinitionDTO toDTO(ProcessDefinition processDefinition) {
        ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
        BeanUtils.copyProperties(processDefinition, processDefinitionDTO);
        ProcessDefinitionEntity processDefinitionEntity =
                (ProcessDefinitionEntity) processDefinition;
        processDefinitionDTO.setTaskDefinitionDTOs(
                processDefinitionEntity
                        .getTaskDefinitions().values().stream().map(td -> {
                    if (Objects.isNull(td.getPriorityExpression()) || !StringUtils.isNumber(td.getPriorityExpression().getExpressionText())) {
                        td.setPriorityExpression(null);
                    }
                    return td;
                }).sorted(Comparator.comparing(TaskDefinition::getPriorityExpression
                        , Comparator.nullsLast(Comparator.comparingInt(o -> Integer.parseInt(o.getExpressionText()))
                        ))).map(ProcessDefinitionConverter::toDTO).collect(Collectors.toList()));
        return processDefinitionDTO;
    }

    public static TaskDefinitionDTO toDTO(TaskDefinition taskDefinition) {
        TaskDefinitionDTO taskDefinitionDTO = new TaskDefinitionDTO();
        if (!Objects.isNull(taskDefinition.getPriorityExpression())
                && StringUtils.isNumber(taskDefinition.getPriorityExpression().getExpressionText())) {
            int priority = Integer.parseInt(taskDefinition.getPriorityExpression().getExpressionText());
            taskDefinitionDTO.setFirstTask(FIRST_TASK_TAG == priority);
        } else {
            taskDefinitionDTO.setFirstTask(false);
        }
        if (!Objects.isNull(taskDefinition.getAssigneeExpression())) {
            String assignee = taskDefinition.getAssigneeExpression().getExpressionText();
            taskDefinitionDTO.setAutoTask(TaskAssignee.RPA.name().equals(assignee));
        } else {
            taskDefinitionDTO.setAutoTask(false);
        }
        taskDefinitionDTO.setKey(taskDefinition.getKey());
        if (!Objects.isNull(taskDefinition.getNameExpression())) {
            taskDefinitionDTO.setName(taskDefinition.getNameExpression().getExpressionText());
        }
        return taskDefinitionDTO;
    }
}
