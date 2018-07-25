package org.hswebframework.web.workflow.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hswebframework.ezorm.core.PropertyWrapper;
import org.hswebframework.ezorm.core.SimplePropertyWrapper;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.workflow.util.QueryUtils;
import org.hswebframework.web.workflow.web.request.ModelCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow/model")
@Api(tags = "工作流-模型管理", description = "工作流模型管理")
@Authorize(permission = "workflow-model", description = "工作流模型管理")
@Slf4j
public class FlowableModelManagerController {

    @Autowired
    private RepositoryService repositoryService;

    private final static String MODEL_ID          = "modelId";
    private final static String MODEL_NAME        = "name";
    private final static String MODEL_REVISION    = "revision";
    private final static String MODEL_DESCRIPTION = "description";
    private final static String MODEL_KEY         = "key";

    @GetMapping
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("获取模型列表")
    public ResponseMessage<PagerResult<Model>> getModelList(QueryParamEntity param) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        return ResponseMessage.ok(
                QueryUtils.doQuery(modelQuery, param,
                        model -> FastBeanCopier.copy(model, new ModelEntity()),
                        (term, modelQuery1) -> {
                            if ("latestVersion".equals(term.getColumn())) {
                                modelQuery1.latestVersion();
                            }
                        }));
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation("创建模型")
    public ResponseMessage<Model> createModel(@RequestBody ModelCreateRequest model) throws Exception {
        JSONObject stencilset = new JSONObject();
        stencilset.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        JSONObject editorNode = new JSONObject();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        editorNode.put("stencilset", stencilset);
        JSONObject modelObjectNode = new JSONObject();
        modelObjectNode.put(MODEL_REVISION, 1);
        modelObjectNode.put(MODEL_DESCRIPTION, model.getDescription());
        modelObjectNode.put(MODEL_KEY, model.getKey());
        modelObjectNode.put(MODEL_NAME, model.getName());

        Model modelData = repositoryService.newModel();
        modelData.setMetaInfo(modelObjectNode.toJSONString());
        modelData.setName(model.getName());
        modelData.setKey(model.getKey());
        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        return ResponseMessage.ok(modelData).status(201);
    }

    @PostMapping("/{modelId}/deploy")
    @ApiOperation("发布模型")
    @Authorize(action = "deploy")
    public ResponseMessage<Deployment> deployModel(@PathVariable String modelId) throws Exception {
        Model modelData = repositoryService.getModel(modelId);
        if (modelData == null) {
            throw new NotFoundException("模型不存在!");
        }
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
    @GetMapping(value = "export/{modelId}/{type}")
    @ApiOperation("导出模型")
    @Authorize(action = "export")
    public void export(@PathVariable("modelId") @ApiParam("模型ID") String modelId,
                       @PathVariable("type") @ApiParam(value = "类型", allowableValues = "bpmn,json", example = "json") String type,
                       @ApiParam(hidden = true) HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for dimension: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();

            if ("bpmn".equals(type)) {

                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);

                filename = mainProcessId + ".bpmn20.xml";
            } else if ("json".equals(type)) {

                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";

            } else {
                throw new UnsupportedOperationException("不支持的格式:" + type);
            }

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

            /*创建输入流*/
            try (ByteArrayInputStream in = new ByteArrayInputStream(exportBytes)) {
                IOUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
                in.close();
            }

        } catch (Exception e) {
            log.error("导出model的xml文件失败：modelId={}, type={}", modelId, type, e);
        }
    }

    @GetMapping(value = "/{modelId}/json")
    @Authorize(action = Permission.ACTION_GET)
    public Object getEditorJson(@PathVariable String modelId) {
        JSONObject modelNode;
        Model model = repositoryService.getModel(modelId);
        if (model == null) throw new NullPointerException("模型不存在");
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

    @PutMapping(value = "/{modelId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Authorize(action = Permission.ACTION_UPDATE)
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

    @DeleteMapping("/{modelId}")
    @Authorize(action = Permission.ACTION_DELETE)
    public ResponseMessage<Void> delete(@PathVariable String modelId) {
        repositoryService.deleteModel(modelId);
        return ResponseMessage.ok();
    }
}