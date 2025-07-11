package org.hswebframework.web.file.service;

import com.google.common.io.Files;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hswebframework.web.file.S3FileProperties;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
public class S3FileStorageService implements FileStorageService {
    
    private final S3FileProperties properties;
    private final S3Client s3Client;
    

    @Override
    public Mono<String> saveFile(FilePart filePart) {
        String filename = buildFileName(filePart.filename());
        
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    InputStream inputStream = dataBuffer.asInputStream(true);
                    return saveFile(inputStream, Files.getFileExtension(filename));
                });
    }


    @Override
    @SneakyThrows
    public Mono<String> saveFile(InputStream inputStream, String fileType) {
        return Mono.fromCallable(() -> {
                    String key = UUID.randomUUID().toString() + (fileType.startsWith(".") ? fileType : "." + fileType);

                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(properties.getBucket())
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
        if (properties.getBaseUrl() != null && !properties.getBaseUrl().isEmpty()) {
            return UriComponentsBuilder
                    .fromUriString(properties.getBaseUrl())
                    .pathSegment(key)
                    .build()
                    .toUriString();
        }
        String host = properties.getBucket() + "." + properties.getEndpoint().replaceFirst("^https?://", "");
        return UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(host)
                .pathSegment(key)
                .build()
                .toUriString();
    }
}
