package org.hswebframework.web.controller.file;

import com.alibaba.fastjson.JSON;
import org.apache.commons.fileupload.ParameterParser;
import org.hswebframework.expands.compress.Compress;
import org.hswebframework.expands.compress.zip.ZIPWriter;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.file.FileInfoService;
import org.hswebframework.web.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * 文件操作控制器，提供文件上传下载等操作
 *
 * @author zhouhao
 * @see FileService
 * @since 3.0
 */
@RestController
@RequestMapping("${hsweb.web.mappings.file:file}")
@Authorize(permission = "file")
@AccessLogger("文件")
@SuppressWarnings("all")
public class FileController {

    private FileService fileService;

    private FileInfoService fileInfoService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Pattern fileNameKeyWordPattern = Pattern.compile("(\\\\)|(/)|(:)(|)|(\\?)|(>)|(<)|(\")");

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    /**
     * 构建并下载zip文件.仅支持POST请求
     *
     * @param name     文件名
     * @param dataStr  数据,jsonArray. 格式:[{"name":"fileName","text":"fileText"}]
     * @param response {@link HttpServletResponse}
     * @throws IOException      写出zip文件错误
     * @throws RuntimeException 构建zip文件错误
     */
    @RequestMapping(value = "/download-zip/{name:.+}", method = {RequestMethod.POST})
    @AccessLogger("下载zip文件")
    @Authorize(action = "download")
    public void downloadZip(@PathVariable("name") String name,
                            @RequestParam("data") String dataStr,
                            HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8"));
        ZIPWriter writer = Compress.zip();
        List<Map<String, String>> data = (List) JSON.parseArray(dataStr, Map.class);
        data.forEach(map -> writer.addTextFile(map.get("name"), map.get("text")));
        writer.write(response.getOutputStream());
    }

    /**
     * 构建一个文本文件,并下载.支持GET,POST请求
     *
     * @param name     文件名
     * @param text     文本内容
     * @param response {@link HttpServletResponse}
     * @throws IOException 写出文本内容错误
     */
    @RequestMapping(value = "/download-text/{name:.+}", method = {RequestMethod.GET, RequestMethod.POST})
    @AccessLogger("下载text文件")
    @Authorize(action = "download")
    public void downloadTxt(@PathVariable("name") String name,
                            @RequestParam("text") String text,
                            HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8"));
        response.getWriter().write(text);
    }

    /**
     * 使用restful风格,通过文件ID下载已经上传的文件,支持断点下载
     * 如: http://host:port/file/download/aSk2a/file.zip 将下载 ID为aSk2a的文件.并命名为file.zip
     *
     * @param id       文件ID
     * @param name     文件名
     * @param response {@link HttpServletResponse}
     * @param request  {@link HttpServletRequest}
     * @return 下载结果, 在下载失败时, 将返回错误信息
     * @throws IOException       读写文件错误
     * @throws NotFoundException 文件不存在
     */
    @RequestMapping(value = "/download/{id}/{name:.+}", method = RequestMethod.GET)
    @AccessLogger("下载文件")
    @Authorize(action = "download")
    public void restDownLoad(@PathVariable("id") String id,
                             @PathVariable("name") String name,
                             HttpServletResponse response,
                             HttpServletRequest request) throws IOException {

        downLoad(id, name, response, request);
    }

    /**
     * 通过文件ID下载已经上传的文件,支持断点下载
     * 如: http://host:port/file/download/aSk2a/file.zip 将下载 ID为aSk2a的文件.并命名为file.zip
     *
     * @param idOrMd5  要下载资源文件的id或者md5值
     * @param name     自定义文件名，该文件名不能存在非法字符.如果此参数为空(null).将使用文件上传时的文件名
     * @param response {@link javax.servlet.http.HttpServletResponse}
     * @param request  {@link javax.servlet.http.HttpServletRequest}
     * @return 下载结果, 在下载失败时, 将返回错误信息
     * @throws IOException                              读写文件错误
     * @throws org.hswebframework.web.NotFoundException 文件不存在
     */
    @GetMapping(value = "/download/{id}")
    @AccessLogger("下载文件")
    @Authorize(action = "download")
    public void downLoad(@PathVariable("id") String idOrMd5,
                         @RequestParam(value = "name", required = false) String name,
                         HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        FileInfoEntity fileInfo = fileInfoService.selectByIdOrMd5(idOrMd5);
        if (fileInfo == null || !DataStatus.STATUS_ENABLED.equals(fileInfo.getStatus())) {
            throw new NotFoundException("文件不存在");
        }
        String fileName = fileInfo.getName();

        String suffix = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf("."), fileName.length()) :
                "";
        //获取contentType
        String contentType = fileInfo.getType() == null ?
                MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName) :
                fileInfo.getType();
        //未自定义文件名，则使用上传时的文件名
        if (StringUtils.isNullOrEmpty(name))
            name = fileInfo.getName();
        //如果未指定文件拓展名，则追加默认的文件拓展名
        if (!name.contains("."))
            name = name.concat(".").concat(suffix);
        //关键字剔除
        name = fileNameKeyWordPattern.matcher(name).replaceAll("");
        int skip = 0;
        long fSize = fileInfo.getSize();
        //尝试判断是否为断点下载
        try {
            //获取要继续下载的位置
            String Range = request.getHeader("Range").replace("bytes=", "").replace("-", "");
            skip = StringUtils.toInt(Range);
        } catch (Exception ignore) {
        }
        response.setContentLength((int) fSize);//文件大小
        response.setContentType(contentType);
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8"));
        //断点下载
        if (skip > 0) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String contentRange = "bytes " + skip + "-" + (fSize - 1) + "/" + fSize;
            response.setHeader("Content-Range", contentRange);
        }
        fileService.writeFile(idOrMd5, response.getOutputStream(), skip);
    }

    /**
     * 上传文件,支持多文件上传.获取到文件流后,调用{@link org.hswebframework.web.service.file.FileService#saveFile(InputStream, String, String, String)}进行文件保存
     * 上传成功后,将返回资源信息如:[{"id":"fileId","name":"fileName","md5":"md5"}]
     *
     * @param files 上传的文件
     * @return 文件上传结果.
     */
    @PostMapping(value = "/upload-multi")
    @AccessLogger("上传多个文件")
    @Authorize(action = "upload")
    public ResponseMessage<List<FileInfoEntity>> upload(@RequestParam("files") MultipartFile[] files) {
        return ResponseMessage.ok(Stream.of(files)
                .map(this::upload)
                .map(ResponseMessage::getResult)
                .collect(Collectors.toList()))
                .include(FileInfoEntity.class,
                        FileInfoEntity.id,
                        FileInfoEntity.name,
                        FileInfoEntity.md5,
                        FileInfoEntity.size,
                        FileInfoEntity.type);
    }

    /**
     * 上传单个文件
     *
     * @param file 上传文件
     * @return 上传结果
     */
    @PostMapping(value = "/upload")
    @AccessLogger("上传文件")
    @Authorize(action = "upload")
    public ResponseMessage<FileInfoEntity> upload(@RequestParam("file") MultipartFile file) {
        List<FileInfoEntity> fileInfoList = new LinkedList<>();
        Authentication authentication = Authentication.current().orElse(null);
        String creator = authentication == null ? null : authentication.getUser().getId();
        if (file.isEmpty()) {
            return ResponseMessage.ok();
        }
        String fileName = file.getOriginalFilename();
        String contentType = Optional.ofNullable(WebUtil.getHttpServletRequest())
                .orElseThrow(UnsupportedOperationException::new)
                .getContentType();
        ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(contentType, ';');
        if (params.get("charset") == null) {
            try {
                fileName = new String(file.getOriginalFilename().getBytes("ISO-8859-1"), "utf-8");
            } catch (@SuppressWarnings("all")UnsupportedEncodingException ignore) {
            }
        }
        if (logger.isInfoEnabled())
            logger.info("start write file:{}", fileName);

        FileInfoEntity fileInfo;
        try {
            fileInfo = fileService.saveFile(file.getInputStream(), fileName, file.getContentType(), creator);
        } catch (IOException e) {
            throw new BusinessException("save file error", e);
        }
        fileInfoList.add(fileInfo);
        return ResponseMessage.ok(fileInfo)
                .include(FileInfoEntity.class, FileInfoEntity.id,
                        FileInfoEntity.name,
                        FileInfoEntity.md5,
                        FileInfoEntity.size,
                        FileInfoEntity.type);
    }

    @PostMapping(value = "/upload-static")
    @AccessLogger("上传静态文件")
    @Authorize(action = "static")
    public ResponseMessage<String> uploadStatic(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseMessage.ok();
        return ResponseMessage.ok(fileService.saveStaticFile(file.getInputStream(), file.getOriginalFilename()));
    }

    @GetMapping(value = "/md5/{md5}")
    @AccessLogger("根据MD5获取文件信息")
    public ResponseMessage<FileInfoEntity> uploadStatic(@PathVariable String md5) throws IOException {
        return ofNullable(fileInfoService.selectByMd5(md5))
                .map(ResponseMessage::ok)
                .orElseThrow(() -> new NotFoundException("file not found"));
    }
}
