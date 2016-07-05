package org.hsweb.web.workflow.controller.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.hsweb.ezorm.meta.expand.PropertyWrapper;
import org.hsweb.ezorm.meta.expand.SimplePropertyWrapper;
import org.hsweb.ezorm.param.TermType;
import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.workflow.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
@AccessLogger("工作流-模型管理")
@Authorize(module = "workflow-model-manager")
public class ActivityModelController extends BasicController {
    @Autowired
    private RepositoryService repositoryService;
    static String MODEL_ID = "modelId";
    static String MODEL_NAME = "name";
    static String MODEL_REVISION = "revision";
    static String MODEL_DESCRIPTION = "description";
    static String MODEL_KEY = "key";

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @Authorize(action = "R")
    public ResponseMessage getModelList(QueryParam param) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        param.getTerms().forEach((term) -> {

            PropertyWrapper valueWrapper = new SimplePropertyWrapper(term.getValue());
            String stringValue = valueWrapper.toString();
            switch (term.getField()) {
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
                    } else if (term.getTermType() == TermType.not) {
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
                .exclude(Model.class, "metaInfo", "persistentState")
                .onlyData();
    }

    @RequestMapping(value = "/model/{modelId}/json", method = RequestMethod.GET)
    @AccessLogger("获取模型定义json")
    @Authorize(action = "R")
    public Object getEditorJson(@PathVariable String modelId) throws Exception {
        JSONObject modelNode;
        Model model = repositoryService.getModel(modelId);
        if (model == null) throw new NotFoundException("表单不存在");
        if (StringUtils.isNotEmpty(model.getMetaInfo())) {
            modelNode = (JSONObject) JSON.parse(model.getMetaInfo());
        } else {
            modelNode = new JSONObject();
            modelNode.put(MODEL_NAME, model.getName());
        }
        modelNode.put(MODEL_ID, model.getId());
        modelNode.put("model", JSON.parse(new String(repositoryService.getModelEditorSource(model.getId()))));
        return modelNode;
    }

    @RequestMapping(value = "/model", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Authorize(action = "C")
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
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(model.getString(MODEL_NAME));
        modelData.setKey(model.getString(MODEL_KEY));
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        return ResponseMessage.created(modelData);
    }

    @RequestMapping(value = "/model/{modelId}/deploy", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @Authorize(action = "deploy")
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

    @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @Authorize(action = "U")
    public void saveModel(@PathVariable String modelId,
                          @RequestParam Map<String, String> values) {
        try {
            Model model = repositoryService.getModel(modelId);
            JSONObject modelJson = JSON.parseObject(model.getMetaInfo());

            modelJson.put(MODEL_NAME, values.get("name"));
            modelJson.put(MODEL_DESCRIPTION, values.get("description"));

            model.setMetaInfo(modelJson.toString());
            model.setName(values.get("name"));

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), values.get("json_xml").getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(values.get("svg_xml").getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();

        } catch (Exception e) {
            throw new ActivitiException("Error saving model", e);
        }
    }


}