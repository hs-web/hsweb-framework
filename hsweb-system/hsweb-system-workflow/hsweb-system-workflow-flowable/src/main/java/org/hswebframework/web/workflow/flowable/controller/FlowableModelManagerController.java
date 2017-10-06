package org.hswebframework.web.workflow.flowable.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.io.IOUtils;
import org.hswebframework.ezorm.core.PropertyWrapper;
import org.hswebframework.ezorm.core.SimplePropertyWrapper;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/workflow/model/")
public class FlowableModelManagerController {

    @Autowired
    private RepositoryService repositoryService;

    private final static String MODEL_ID          = "modelId";
    private final static String MODEL_NAME        = "name";
    private final static String MODEL_REVISION    = "revision";
    private final static String MODEL_DESCRIPTION = "description";
    private final static String MODEL_KEY         = "key";

    @GetMapping("getModelList")
    public ResponseMessage<PagerResult<Model>> getModelList(QueryParamEntity param) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        param.getTerms().forEach((term) -> {

            PropertyWrapper valueWrapper = new SimplePropertyWrapper(term.getValue());
            String stringValue = valueWrapper.toString();
            switch (term.getColumn()) {
                case "name":
                    if (term.getTermType().equals(TermType.like))
                        modelQuery.modelNameLike(stringValue);
                    else
                        modelQuery.modelName(stringValue);
                    break;
                case "key":
                    modelQuery.modelKey(stringValue);
                    break;
                case "category":
                    if (term.getTermType().equals(TermType.like)) {
                        modelQuery.modelCategoryLike(stringValue);
                    } else if (term.getTermType().equals(TermType.not)) {
                        modelQuery.modelCategoryNotEquals(stringValue);
                    } else
                        modelQuery.modelCategory(stringValue);
                    break;
                case "tenantId":
                    if (term.getTermType().equals(TermType.like))
                        modelQuery.modelTenantIdLike(stringValue);
                    else
                        modelQuery.modelTenantId(stringValue);
                    break;
                case "version":
                    if ("latest".equals(stringValue))
                        modelQuery.latestVersion();
                    else
                        modelQuery.modelVersion(valueWrapper.toInt());
            }
        });
        modelQuery.orderByCreateTime().desc();
        int total = (int) modelQuery.count();
        param.rePaging(total);
        List<Model> models = modelQuery.listPage(param.getPageIndex(), param.getPageSize() * (param.getPageIndex() + 1));
        return ResponseMessage.ok(new PagerResult<>(total, models))
                .exclude(Model.class, "metaInfo", "persistentState");
    }

    @PostMapping("createModel")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseMessage createModel(@RequestBody JSONObject model) throws Exception {
        JSONObject stencilset = new JSONObject();
        stencilset.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        JSONObject editorNode = new JSONObject();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        editorNode.put("stencilset", stencilset);
        JSONObject modelObjectNode = new JSONObject();
        modelObjectNode.put(MODEL_REVISION, 1);
        modelObjectNode.put(MODEL_DESCRIPTION, model.getString(MODEL_DESCRIPTION));
        modelObjectNode.put(MODEL_KEY, model.getString(MODEL_KEY));
        modelObjectNode.put(MODEL_NAME, model.getString(MODEL_NAME));
        modelObjectNode.put(MODEL_DESCRIPTION, model.getString(MODEL_DESCRIPTION));

        Model modelData = repositoryService.newModel();
        modelData.setMetaInfo(modelObjectNode.toJSONString());
        modelData.setName(model.getString(MODEL_NAME));
        modelData.setKey(model.getString(MODEL_KEY));
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        return ResponseMessage.ok(modelData);
    }

    @PutMapping("{modelId}/deploy")
    public ResponseMessage deployModel(@PathVariable String modelId) throws Exception {
        Model modelData = repositoryService.getModel(modelId);
        if (modelData == null) throw new NotFoundException("模型不存在!");
        ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "utf8"))
                .deploy();
        return ResponseMessage.ok(deployment).include(Deployment.class, "id", "name", "new");
    }

    /**
     * 导出model对象为指定类型
     *
     * @param modelId 模型ID
     * @param type    导出文件类型(bpmn\json)
     */
    @RequestMapping(value = "export/{modelId}/{type}", method = RequestMethod.GET)
    public void export(@PathVariable("modelId") String modelId,
                       @PathVariable("type") String type,
                       HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for type: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();

            if (type.equals("bpmn")) {

                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);

                filename = mainProcessId + ".bpmn20.xml";
            } else if (type.equals("json")) {

                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";

            }

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

            /*创建输入流*/
            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());

            response.flushBuffer();
            in.close();
        } catch (Exception e) {
//            logger.error("导出model的xml文件失败：modelId={}, type={}", modelId, type, e);
        }
    }

}