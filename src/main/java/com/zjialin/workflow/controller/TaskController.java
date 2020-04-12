package com.zjialin.workflow.controller;

import com.zjialin.workflow.utils.RestMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zjialin<br>
 * @version 1.0<br>
 * @createDate 2019/08/30 11:59 <br>
 * @Description <p> 任务相关接口 </p>
 */

@RestController
@RequestMapping(value = "/v2/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "任务相关接口")
@Slf4j
public class TaskController extends BaseController {

    @GetMapping(path = "findTaskByAssignee")
    @ApiOperation(value = "根据流程assignee查询当前人的个人任务", notes = "根据流程assignee查询当前人的个人任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "assignee", value = "代理人（当前用户）", dataType = "String", paramType = "query"),
    })
    public RestMessage findTaskByAssignee(@RequestParam("assignee") String assignee,
                                          @RequestParam("processDefinitionKey") String processDefinitionKey,
                                          @RequestParam("page") Integer page,
                                          @RequestParam("size") Integer size) {
        RestMessage restMessage;

        try {
            //指定个人任务查询
            TaskQuery query = taskService.createTaskQuery().taskAssignee(assignee).processDefinitionKey(processDefinitionKey);
            List<Task> taskList = query.listPage(page, size);
            Long count = query.count();

//            TaskPageVO result = new TaskPageVO();
//            result.setTasks(taskList);
//            result.setCount(count);
//            restMessage = RestMessage.success("查询成功", result);
            if (CollectionUtil.isNotEmpty(taskList)) {
                List<Map<String, String>> resultList = new ArrayList<>();
                for (Task task : taskList) {
                    Map<String, String> resultMap = new HashMap<>();
                    /* 任务ID */
                    resultMap.put("taskID", task.getId());
                    /* 任务名称 */
                    resultMap.put("taskName", task.getName());
                    /* 任务的创建时间 */
                    resultMap.put("taskCreateTime", task.getCreateTime().toString());
                    /* 任务的办理人 */
                    resultMap.put("taskAssignee", task.getAssignee());
                    /* 流程实例ID */
                    resultMap.put("processInstanceId", task.getProcessInstanceId());
                    /* 执行对象ID */
                    resultMap.put("executionId", task.getExecutionId());
                    /* 流程定义ID */
                    resultMap.put("processDefinitionId", task.getProcessDefinitionId());
//                    resultMap.put("businessKey", task.get)
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                    if (Objects.nonNull(processInstance)) {
                        resultMap.put("businessKey", processInstance.getBusinessKey());
                    }
                    resultList.add(resultMap);
                }
//                restMessage = RestMessage.success("查询成功", resultList);
                Map<String, Object> result = new HashMap<>();
                result.put("list", resultList);
                result.put("total", count);
                restMessage = RestMessage.success("查询成功", result);

            } else {
                restMessage = RestMessage.success("查询成功", null);
            }
        } catch (Exception e) {
            restMessage = RestMessage.fail("查询失败", e.getMessage());
            log.error("根据流程assignee查询当前人的个人任务,异常:{}", e);
            return restMessage;
        }
        return restMessage;
    }

    @PostMapping(path = "setOwner")
    @ApiOperation(value = "设置组长为办理人", notes = "设置组长为办理人")
    public RestMessage setOwner(@RequestParam("processId") String processId,
                                @RequestBody Map<String, Object> map) {
        RestMessage restMessage;

        try {
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(processId).list();
            if (CollectionUtil.isNotEmpty(taskList)) {
                Task task = taskList.get(0);
                taskService.setOwner(task.getId(), (String) map.get("owner"));
                taskService.setAssignee(task.getId(), (String) map.get("assignee"));
                restMessage = RestMessage.success("设置成功", null);
            } else {
                restMessage = RestMessage.success("设置成功", null);
            }
        } catch (Exception e) {
            restMessage = RestMessage.fail("设置成功", e.getMessage());
            log.error("设置办理人,异常:{}", e);
            return restMessage;
        }
        return restMessage;
    }


    @PostMapping(path = "completeTask")
    @ApiOperation(value = "完成任务", notes = "完成任务，任务进入下一个节点")
    public RestMessage completeTask(@RequestParam("taskId") String taskId, @RequestBody Map<String, Object> variables) {

        RestMessage restMessage;
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            taskService.complete(taskId, variables);
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            restMessage = RestMessage.fail("完成任务成功", taskId);
        } catch (Exception e) {
            restMessage = RestMessage.fail("完成任务失败", e.getMessage());
            log.error("完成任务,异常:{}", e);
        }
        return restMessage;
    }


    @PostMapping(path = "turnTask")
    @ApiOperation(value = "任务转办", notes = "任务转办，把任务交给别人处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "userKey", value = "用户Key", dataType = "String", paramType = "query"),
    })
    public RestMessage turnTask(@RequestParam("taskId") String taskId, @RequestParam("userKey") String userKey) {
        RestMessage restMessage = new RestMessage();
        try {
            taskService.setAssignee(taskId, userKey);
            restMessage = RestMessage.fail("完成任务成功", taskId);
        } catch (Exception e) {
            restMessage = RestMessage.fail("完成任务失败", e.getMessage());
            log.error("任务转办,异常:{}", e);
        }
        return restMessage;
    }
}
