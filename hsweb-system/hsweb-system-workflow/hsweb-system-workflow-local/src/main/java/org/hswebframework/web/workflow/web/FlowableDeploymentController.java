package org.hswebframework.web.workflow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.FilenameUtils;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.service.BpmActivityService;
import org.hswebframework.web.workflow.service.BpmProcessService;
import org.hswebframework.web.workflow.service.BpmTaskService;
import org.hswebframework.web.workflow.service.imp.AbstractFlowableService;
import org.hswebframework.web.workflow.util.QueryUtils;
import org.hswebframework.web.workflow.web.response.ActivityInfo;
import org.hswebframework.web.workflow.web.response.ProcessDefinitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipInputStream;

/**
 * @Author wangwei
 * @Date 2017/8/10.
 */
@RestController
@RequestMapping("/workflow/process/definition")
@Api(tags = "工作流-流程定义管理", description = "工作流流程定义管理")
@Authorize(permission = "workflow-definition", description = "工作流-流程定义管理")
public class FlowableDeploymentController extends AbstractFlowableService {

    private final static String MODEL_ID = "modelId";

    @Autowired
    BpmTaskService     bpmTaskService;
    @Autowired
    BpmProcessService  bpmProcessService;
    @Autowired
    BpmActivityService bpmActivityService;

    /**
     * 流程定义列表
     */
    @GetMapping
    @ApiOperation("查询流程定义列表")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<PagerResult<ProcessDefinitionInfo>> queryProcessList(QueryParamEntity param) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        return ResponseMessage.ok(QueryUtils.doQuery(processDefinitionQuery, param, ProcessDefinitionInfo::of));
    }

    /**
     * 部署流程资源
     * 加载ZIP文件中的流程
     */
    @PostMapping(value = "/deploy")
    @ApiOperation("上传流程定义文件并部署流程")
    @Authorize(action = "deploy")
    public ResponseMessage<Deployment> deploy(@RequestPart(value = "file") MultipartFile file) throws IOException {
        // 获取上传的文件名
        String fileName = file.getOriginalFilename();

        // 得到输入流（字节流）对象
        InputStream fileInputStream = file.getInputStream();

        // 文件的扩展名
        String extension = FilenameUtils.getExtension(fileName);

        // zip或者bar类型的文件用ZipInputStream方式部署
        DeploymentBuilder deployment = repositoryService.createDeployment();
        if ("zip".equals(extension) || "bar".equals(extension)) {
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            deployment.addZipInputStream(zip);
        } else {
            // 其他类型的文件直接部署
            deployment.addInputStream(fileName, fileInputStream);
        }
        Deployment result = deployment.deploy();

        return ResponseMessage.ok(result);
    }

    /**
     * 读取流程资源
     *
     * @param processDefinitionId 流程定义ID
     * @param resourceName        资源名称
     */
    @GetMapping(value = "/{processDefinitionId}/resource/{resourceName}")
    @ApiOperation("读取流程资源")
    @Authorize(action = Permission.ACTION_QUERY)
    @SneakyThrows
    public void readResource(@PathVariable String processDefinitionId
            , @PathVariable String resourceName, HttpServletResponse response) {
        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition pd = pdq.processDefinitionId(processDefinitionId).singleResult();

        // 通过接口读取
        try (InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName)) {
            StreamUtils.copy(resourceAsStream, response.getOutputStream());
        }

    }

    /***
     * 流程定义转换Model
     */
    @PutMapping(value = "/convert-to-model/{processDefinitionId}")
    @ApiOperation("流程定义转换模型")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<String> convertToModel(@PathVariable("processDefinitionId") String processDefinitionId)
            throws UnsupportedEncodingException, XMLStreamException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (null == processDefinition) {
            throw new NotFoundException();
        }
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());

        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);


        BpmnJsonConverter converter = new BpmnJsonConverter();
        com.fasterxml.jackson.databind.node.ObjectNode modelNode = converter.convertToJson(bpmnModel);
        org.activiti.engine.repository.Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName().substring(0, processDefinition.getResourceName().indexOf(".")));
        modelData.setCategory(processDefinition.getDeploymentId());

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
        return ResponseMessage.ok(modelData.getId());
    }

    /**
     * 删除部署的流程,如果流程下有正在运行的流程实例则报错
     *
     * @param deploymentId 流程部署ID
     */
    @DeleteMapping(value = "/deployment/{deploymentId}")
    @ApiOperation("删除部署的流程")
    @Authorize(action = Permission.ACTION_DELETE)
    public ResponseMessage<Void> deleteProcessDefinition(
            @PathVariable("deploymentId") String deploymentId
            , @RequestParam(defaultValue = "false") boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
        
        return ResponseMessage.ok();
    }


    /**
     * 查看当前节点流程图
     */
    @GetMapping("/{processInstanceId}/activity")
    @ApiOperation("查看当前流程活动节点流程图")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<ActivityInfo> getProcessInstanceActivity(@PathVariable String processInstanceId) {
        HistoricProcessInstance processInstance = bpmTaskService.selectHisProInst(processInstanceId);
        if (processInstance != null) {
            ActivityImpl activity = bpmActivityService.getActivityByProcInstId(processInstance.getProcessDefinitionId(), processInstance.getId());
            return ResponseMessage.ok(ActivityInfo.of(activity));
        } else {
            throw new NotFoundException("流程不存在");
        }
    }

    @GetMapping("/{processInstanceId}/image")
    @ApiOperation("查看当前流程活动节点流程图")
    @Authorize(action = Permission.ACTION_QUERY)
    public void getProcessImage(@PathVariable String processInstanceId, HttpServletResponse response) throws IOException {
        try (InputStream inputStream = bpmProcessService.findProcessPic(processInstanceId)) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            ImageIO.write(ImageIO.read(inputStream), "png", response.getOutputStream());
        }
    }
}
