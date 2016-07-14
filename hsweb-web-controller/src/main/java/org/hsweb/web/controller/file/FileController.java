/*
 * Copyright 2015-2016 https://github.com/hs-web
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.controller.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.compress.Compress;
import org.hsweb.expands.compress.zip.ZIPWriter;
import org.hsweb.expands.office.excel.ExcelIO;
import org.hsweb.expands.office.excel.config.Header;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.resource.FileService;
import org.hsweb.web.service.resource.ResourcesService;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文件管理控制器，用于上传和下载资源文件
 */
@RestController
@RequestMapping(value = "/file")
@AccessLogger("文件管理")
@Authorize
public class FileController {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ResourcesService resourcesService;

    @Resource
    private FileService fileService;

    //文件名中不允许出现的字符 \ / : | ? < > "
    private static final Pattern fileNameKeyWordPattern = Pattern.compile("(\\\\)|(/)|(:)(|)|(\\?)|(>)|(<)|(\")");

    private static final Map<String, String> mediaTypeMapper = new HashMap<>();

    static {
        mediaTypeMapper.put(".png", MediaType.IMAGE_PNG_VALUE);
        mediaTypeMapper.put(".jpg", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".jpeg", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".gif", MediaType.IMAGE_GIF_VALUE);
        mediaTypeMapper.put(".bmp", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".json", MediaType.APPLICATION_JSON_VALUE);
        mediaTypeMapper.put(".txt", MediaType.TEXT_PLAIN_VALUE);
        mediaTypeMapper.put(".css", MediaType.TEXT_PLAIN_VALUE);
        mediaTypeMapper.put(".js", "application/javascript");
        mediaTypeMapper.put(".html", MediaType.TEXT_HTML_VALUE);
        mediaTypeMapper.put(".xml", MediaType.TEXT_XML_VALUE);
    }

    /**
     * 构建并下载excel,
     *
     * @param name       excel文件名
     * @param headerJson 表头配置JSON 格式:{@link Header}
     * @param dataJson   数据JSON 格式:{@link List<Map<String,Object>}
     * @param response   {@link HttpServletResponse}
     * @throws Exception   构建excel异常
     * @throws IOException 写出excel异常
     */
    @RequestMapping(value = "/download/{name}.xlsx", method = {RequestMethod.POST})
    @AccessLogger("下载excel文件")
    public void downloadExcel(@PathVariable("name") String name,
                              @RequestParam("header") String headerJson,
                              @RequestParam("data") String dataJson,
                              HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8") + ".xlsx");
        List<Header> headers = JSON.parseArray(headerJson, Header.class);
        List<Map> datas = JSON.parseArray(dataJson, Map.class);
        ExcelIO.write(response.getOutputStream(), headers, (List) datas);
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
    public ResponseMessage restDownLoad(@PathVariable("id") String id,
                                        @PathVariable("name") String name,
                                        HttpServletResponse response, HttpServletRequest request) throws IOException {
        return downLoad(id, name, response, request);
    }

    /**
     * 通过文件ID下载已经上传的文件,支持断点下载
     * 如: http://host:port/file/download/aSk2a/file.zip 将下载 ID为aSk2a的文件.并命名为file.zip
     *
     * @param id       要下载资源文件的id
     * @param name     自定义文件名，该文件名不能存在非法字符.如果此参数为空(null).将使用文件上传时的文件名
     * @param response {@link HttpServletResponse}
     * @param request  {@link HttpServletRequest}
     * @return 下载结果, 在下载失败时, 将返回错误信息
     * @throws IOException       读写文件错误
     * @throws NotFoundException 文件不存在
     */
    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @AccessLogger("下载文件")
    public ResponseMessage downLoad(@PathVariable("id") String id,
                                    @RequestParam(value = "name", required = false) String name,
                                    HttpServletResponse response, HttpServletRequest request) throws IOException {
        Resources resources = resourcesService.selectByPk(id);
        if (resources == null || resources.getStatus() != 1) {
            throw new NotFoundException("文件不存在");
        } else {
            if (!"file".equals(resources.getType()))
                throw new NotFoundException("文件不存在");
            String fileBasePath = fileService.getFileBasePath();
            File file = new File(fileBasePath.concat(resources.getPath().concat("/".concat(resources.getMd5()))));
            if (!file.canRead()) {
                throw new NotFoundException("文件不存在");
            }
            //获取contentType，默认application/octet-stream
            String contentType = mediaTypeMapper.get(resources.getSuffix().toLowerCase());
            if (contentType == null)
                contentType = "application/octet-stream";
            //未自定义文件名，则使用上传时的文件名
            if (StringUtils.isNullOrEmpty(name))
                name = resources.getName();
            //如果未指定文件拓展名，则追加默认的文件拓展名
            if (!name.contains("."))
                name = name.concat(".").concat(resources.getSuffix());
            //关键字剔除
            name = fileNameKeyWordPattern.matcher(name).replaceAll("");
            int skip = 0;
            long fSize = file.length();
            //尝试判断是否为断点下载
            try {
                //获取要继续下载的位置
                String Range = request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", "");
                skip = StringUtils.toInt(Range);
            } catch (Exception e) {
            }
            response.setContentLength((int) fSize);//文件大小
            response.setContentType(contentType);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8"));
            //try with resource
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                 BufferedOutputStream stream = new BufferedOutputStream(response.getOutputStream())) {
                //断点下载
                if (skip > 0) {
                    inputStream.skip(skip);
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    String contentRange = new StringBuffer("bytes ").append(skip).append("-").append(fSize - 1).append("/").append(fSize).toString();
                    response.setHeader("Content-Range", contentRange);
                }
                byte b[] = new byte[2048 * 10];
                while ((inputStream.read(b)) != -1) {
                    stream.write(b);
                }
                stream.flush();
            } catch (IOException e) {
                logger.debug(String.format("download file error%s", e.getMessage()));
                throw e;
            }
            return null;
        }

    }

    /**
     * 上传文件,支持多文件上传.获取到文件流后,调用{@link FileService#saveFile(InputStream, String)}进行文件保存
     * 上传成功后,将返回资源信息如:[{"id":"fileId","name":"fileName","md5":"md5"}]
     *
     * @param files 文件列表
     * @return 文件上传结果.
     * @throws IOException 保存文件错误
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @AccessLogger("上传文件")
    public ResponseMessage upload(@RequestParam("file") MultipartFile[] files) throws IOException {
        if (logger.isInfoEnabled())
            logger.info(String.format("start upload , file number:%s", files.length));
        List<Resources> resourcesList = new LinkedList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (!file.isEmpty()) {
                if (logger.isInfoEnabled())
                    logger.info("start write file:{}", file.getOriginalFilename());
                String fileName = file.getOriginalFilename();
                Resources resources = fileService.saveFile(file.getInputStream(), fileName);
                resourcesList.add(resources);
            }
        }//响应上传成功的资源信息
        return ResponseMessage.ok(resourcesList)
                .include(Resources.class, "id", "name", "md5");
    }

}
