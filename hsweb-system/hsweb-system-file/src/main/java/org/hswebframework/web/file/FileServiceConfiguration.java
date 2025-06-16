package org.hswebframework.web.file;

import org.hswebframework.web.file.service.FileStorageService;
import org.hswebframework.web.file.service.LocalFileStorageService;
import org.hswebframework.web.file.service.S3FileStorageService;
import org.hswebframework.web.file.web.ReactiveFileController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@AutoConfiguration
@EnableConfigurationProperties({FileUploadProperties.class,S3StorageProperties.class})
public class FileServiceConfiguration {


    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveConfiguration {

        @Bean
        @ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
        public FileStorageService fileStorageService(FileUploadProperties properties) {
            return new LocalFileStorageService(properties);
        }

        @Bean
        @ConditionalOnProperty(name = "file.storage", havingValue = "s3")
        public ReactiveFileController reactiveFileController(FileUploadProperties properties,
                                                             FileStorageService storageService) {
            return new ReactiveFileController(properties, storageService);
        }




        @Bean
        @ConditionalOnMissingBean(FileStorageService.class)
        public FileStorageService s3FileStorageService(S3StorageProperties properties,
                                                         S3Client s3Client) {
            return new S3FileStorageService(properties, s3Client);
        }

        @Bean
        @ConditionalOnProperty(name = "file.storage", havingValue = "s3")
        public S3Client s3Client(S3StorageProperties properties) {
            return S3Client.builder()
                    .endpointOverride(URI.create(properties.getEndpoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                            properties.getAccessKey(), properties.getSecretKey()
                    )))
                    .region(Region.of(properties.getRegion()))
                    .build();
        }

    }

}
