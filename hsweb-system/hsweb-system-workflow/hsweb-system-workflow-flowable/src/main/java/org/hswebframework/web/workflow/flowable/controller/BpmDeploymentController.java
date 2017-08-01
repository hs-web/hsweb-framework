package org.hswebframework.web.workflow.flowable.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.constants.ModelDataJsonConstants;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.hsweb.ezorm.core.PropertyWrapper;
import org.hsweb.ezorm.core.SimplePropertyWrapper;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @Author wangwei
 * @Date 2017/7/31.
 */
@RestController
@RequestMapping("/workflow/definition")
public class BpmDeploymentController extends FlowableAbstract {

    /**
     * 流程定义列表
     */
    @GetMapping("/list")
    public ResponseMessage<PagerResult<ProcessDefinition>> processList(QueryParamEntity param) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        param.getTerms().forEach((term) -> {

            PropertyWrapper valueWrapper = new SimplePropertyWrapper(term.getValue());
            String stringValue = valueWrapper.toString();
            switch (term.getColumn()) {
                case "name":
                    if (term.getTermType().equals(TermType.like)) {
                        processDefinitionQuery.processDefinitionNameLike(stringValue);
                    } else
                        processDefinitionQuery.processDefinitionName(stringValue);
                    break;
                case "key":
                    if (term.getTermType().equals(TermType.like)) {
                        processDefinitionQuery.processDefinitionKeyLike(stringValue);
                    } else
                        processDefinitionQuery.processDefinitionKey(stringValue);
                    break;
                case "category":
                    if (term.getTermType().equals(TermType.like))
                        processDefinitionQuery.processDefinitionCategoryLike(stringValue);
                    else
                        processDefinitionQuery.processDefinitionCategory(stringValue);
                    break;
                case "deploymentId":
                    processDefinitionQuery.deploymentId(stringValue);
                    break;
            }
        });
        int total = (int) processDefinitionQuery.count();
        param.rePaging(total);
        List<ProcessDefinition> models = processDefinitionQuery.listPage(param.getPageIndex(), param.getPageSize() * (param.getPageIndex() + 1));
        return ResponseMessage.ok(new PagerResult<>(total, models))
                .exclude(ProcessDefinitionEntity.class,"identityLinks");
    }

    /**
     * 部署流程资源
     * 加载ZIP文件中的流程
     */
    @PostMapping(value="/deploy")
    public Object deploy(@RequestParam(value = "file", required = true) MultipartFile file) {
        JSONObject modelNode = new JSONObject();
        modelNode.put("succ","false");
        // 获取上传的文件名
        String fileName = file.getOriginalFilename();

        try {
            // 得到输入流（字节流）对象
            InputStream fileInputStream = file.getInputStream();

            // 文件的扩展名
            String extension = FilenameUtils.getExtension(fileName);

            // zip或者bar类型的文件用ZipInputStream方式部署
            DeploymentBuilder deployment = repositoryService.createDeployment();
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment.addZipInputStream(zip);
            } else {
                // 其他类型的文件直接部署
                deployment.addInputStream(fileName, fileInputStream);
            }
            deployment.deploy();
            modelNode.put("message","流程部署完成");
        } catch (Exception e) {
            modelNode.put("message","流程部署失败,获取文件流失败");
        }

        return modelNode;
    }

    /**
     * 读取流程资源
     *
     * @param processDefinitionId 流程定义ID
     * @param resourceName        资源名称
     */
    @GetMapping(value = "/read-resource/{processDefinitionId}/{resourceName}")
    public void readResource(@PathVariable String processDefinitionId, @PathVariable String resourceName, HttpServletResponse response)
            throws Exception {
        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition pd = pdq.processDefinitionId(processDefinitionId).singleResult();

        // 通过接口读取
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /***
     * 流程定义转换Model
     * @param processDefinitionId
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    @PutMapping(value = "/convert-to-model/{processDefinitionId}")
    public Object convertToModel(@PathVariable String processDefinitionId)
            throws UnsupportedEncodingException, XMLStreamException {
        JSONObject jsonObject = new JSONObject();
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId).singleResult();
            InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                    processDefinition.getResourceName());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            BpmnJsonConverter converter = new BpmnJsonConverter();
            com.fasterxml.jackson.databind.node.ObjectNode modelNode = converter.convertToJson(bpmnModel);
            org.flowable.engine.repository.Model modelData = repositoryService.newModel();
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
            jsonObject.put("succ",true);
            jsonObject.put("message","转换完成");
        }catch (Exception e){
            jsonObject.put("succ",false);
            jsonObject.put("message","转换失败");
        }
        return jsonObject;
    }

    /**
     * 删除部署的流程,如果流程下有正在运行的流程实例则报错
     *
     * @param deploymentId 流程部署ID
     */
    @PutMapping(value = "/delete-deployment/{deploymentId}")
    @ResponseStatus(value = HttpStatus.OK)
    public Object deleteProcessDefinition(@PathVariable String deploymentId) {
        JSONObject jsonObject = new JSONObject();
        try{
            repositoryService.deleteDeployment(deploymentId);
            jsonObject.put("succ",true);
        }catch (Exception e){
            jsonObject.put("succ",false);
        }
        return jsonObject;
    }

    /**
     * 删除部署的流程，级联删除流程实例
     *
     * @param deploymentId 流程部署ID
     */
    @PutMapping(value = "/delete-deploy/{deploymentId}")
    @ResponseStatus(value = HttpStatus.OK)
    public Object deleteProcess(@PathVariable String deploymentId) {
        JSONObject jsonObject = new JSONObject();
        try{
            repositoryService.deleteDeployment(deploymentId,true);
            jsonObject.put("succ",true);
        }catch (Exception e){
            jsonObject.put("succ",false);
        }
        return jsonObject;
    }
}
