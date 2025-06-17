package org.hswebframework.web.file;

import org.hswebframework.web.file.service.FileStorageService;
import org.hswebframework.web.file.service.S3FileStorageService;
import org.hswebframework.web.file.web.ReactiveFileController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(name = "hsweb.file.storage", havingValue = "s3", matchIfMissing = false)
@EnableConfigurationProperties({S3FileProperties.class, FileUploadProperties.class})
public class S3FileStorageConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(S3FileProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public FileStorageService s3FileStorageService(FileUploadProperties uploadProperties,
                                                   S3FileProperties s3Properties,
                                                   S3Client s3Client) {
        return new S3FileStorageService(uploadProperties, s3Properties, s3Client);
    }

    @Bean
    @ConditionalOnMissingBean(name = "reactiveFileController")
    public ReactiveFileController reactiveFileController(FileUploadProperties properties,
                                                         FileStorageService storageService) {
        return new ReactiveFileController(properties, storageService);
    }
}
