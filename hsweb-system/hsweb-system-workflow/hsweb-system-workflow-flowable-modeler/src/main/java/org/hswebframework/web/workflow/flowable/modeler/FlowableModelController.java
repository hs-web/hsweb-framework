package org.hswebframework.web.workflow.flowable.modeler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.batik.transcoder.TranscoderException;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.hsweb.ezorm.core.PropertyWrapper;
import org.hsweb.ezorm.core.SimplePropertyWrapper;
import org.hsweb.ezorm.core.param.TermType;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
@Authorize(permission = "workflow-model-manager")
public class FlowableModelController {
    @Autowired
    private RepositoryService repositoryService;

    private final static String MODEL_ID = "modelId";
    private final static String MODEL_NAME = "name";
    private final static String MODEL_REVISION = "revision";
    private final static String MODEL_DESCRIPTION = "description";
    private final static String MODEL_KEY = "key";

    @RequestMapping(value = "/models", method = RequestMethod.GET)
    @Authorize(action = "R")
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

    @RequestMapping(value = "/model/{modelId}/json", method = RequestMethod.GET)
    @AccessLogger("获取模型定义json")
    @Authorize(action = "R")
    public Object getEditorJson(@PathVariable String modelId) throws Exception {
        JSONObject modelNode;
        Model model = repositoryService.getModel(modelId);
        if (model == null) throw new NotFoundException("模型不存在");
        if (StringUtils.isNotEmpty(model.getMetaInfo())) {
            modelNode = JSON.parseObject(model.getMetaInfo());
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
        modelData.setMetaInfo(modelObjectNode.toJSONString());
        modelData.setName(model.getString(MODEL_NAME));
        modelData.setKey(model.getString(MODEL_KEY));
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        return ResponseMessage.ok(modelData);
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
                          @RequestParam Map<String, String> values) throws TranscoderException, IOException {
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

    }


}