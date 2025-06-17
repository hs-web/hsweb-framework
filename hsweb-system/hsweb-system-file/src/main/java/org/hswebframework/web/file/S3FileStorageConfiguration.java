package org.hswebframework.web.file;

import org.hswebframework.web.file.service.FileStorageService;
import org.hswebframework.web.file.service.S3FileStorageService;
import org.hswebframework.web.file.web.S3FileController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnProperty(name = "file.storage", havingValue = "s3", matchIfMissing = false)
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3FileStorageConfiguration {

    @Bean
    @ConditionalOnBean(S3StorageProperties.class)
    @ConditionalOnMissingBean(name = "s3FileController")
    public S3FileController s3FileController(S3StorageProperties properties,
                                                         FileStorageService storageService) {
        return new S3FileController(properties, storageService);
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(S3StorageProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public FileStorageService s3FileStorageService(S3StorageProperties properties, S3Client s3Client) {
        return new S3FileStorageService(properties, s3Client);
    }
}
