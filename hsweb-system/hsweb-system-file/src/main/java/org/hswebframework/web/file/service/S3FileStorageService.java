package org.hswebframework.web.file.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.file.FileUploadProperties;
import org.hswebframework.web.file.S3FileProperties;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final FileUploadProperties fileProperties;
    private final S3FileProperties s3Properties;
    private final S3Client s3Client;
    

    @Override
    public Mono<String> saveFile(FilePart filePart) {
        String filename = buildFileName(filePart.filename());
        return DataBufferUtils.join(filePart.content())
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new ByteArrayInputStream(bytes);
                })
                .map(inputStream -> {
                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(filename)
                            .build();

                    s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
                    return buildFileUrl(filename);
                });
    }


    @Override
    @SneakyThrows
    public Mono<String> saveFile(InputStream inputStream, String fileType) {
        return Mono.fromCallable(() -> {
                    String key = UUID.randomUUID().toString() + (fileType.startsWith(".") ? fileType : "." + fileType);

                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(key)
                            .build();

                    s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
                    return buildFileUrl(key);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String buildFileName(String originalName) {
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + suffix.toLowerCase(Locale.ROOT);
    }

    private String buildFileUrl(String key) {
        if (s3Properties.getBaseUrl() != null && !s3Properties.getBaseUrl().isEmpty()) {
            return s3Properties.getBaseUrl() + "/" + key;
        }
        return "https://" + s3Properties.getBucket() + "." + s3Properties.getEndpoint().replace("https://", "").replace("http://", "") + "/" + key;
    }
}
