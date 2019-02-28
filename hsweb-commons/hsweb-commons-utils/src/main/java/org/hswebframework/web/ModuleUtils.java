package org.hswebframework.web;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 3.0.6
 */
@Slf4j
public abstract class ModuleUtils {

    private ModuleUtils() {

    }

    private final static Map<Class, ModuleInfo> classModuleInfoRepository;

    private final static Map<String, ModuleInfo> nameModuleInfoRepository;

    static {
        classModuleInfoRepository = new ConcurrentHashMap<>();
        nameModuleInfoRepository = new ConcurrentHashMap<>();
        try {
            log.info("init module info");
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:/hsweb-module.json");
            for (Resource resource : resources) {
                String classPath = getClassPath(resource.getURL().toString(), "hsweb-module.json");
                ModuleInfo moduleInfo = JSON.parseObject(resource.getInputStream(), ModuleInfo.class);
                moduleInfo.setClassPath(classPath);
                ModuleUtils.register(moduleInfo);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static ModuleInfo getModuleByClass(Class type) {
        return classModuleInfoRepository.computeIfAbsent(type, ModuleUtils::parse);
    }

    public static String getClassPath(Class type) {
        ProtectionDomain domain = type.getProtectionDomain();
        CodeSource codeSource = domain.getCodeSource();
        if (codeSource == null) {
            return getClassPath(type.getResource("").getPath(), type.getPackage().getName());
        }
        String path = codeSource.getLocation().toString();

        boolean isJar = path.contains("!/") && path.contains(".jar");

        if (isJar) {
            return path.substring(0, path.lastIndexOf(".jar") + 4);
        }

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String getClassPath(String path, String packages) {
        if (path.endsWith(".jar")) {
            return path;
        }
        boolean isJar = path.contains("!/") && path.contains(".jar");

        if (isJar) {
            return path.substring(0, path.lastIndexOf(".jar") + 4);
        }

        int pos = path.endsWith("/") ? 2 : 1;
        return path.substring(0, path.length() - packages.length() - pos);
    }

    private static ModuleInfo parse(Class type) {
        String classpath = getClassPath(type);
        return nameModuleInfoRepository.values()
                .stream()
                .filter(moduleInfo -> classpath.equals(moduleInfo.classPath))
                .findFirst()
                .orElse(noneInfo);
    }

    public static ModuleInfo getModule(String id) {
        return nameModuleInfoRepository.get(id);
    }

    public static void register(ModuleInfo moduleInfo) {
        nameModuleInfoRepository.put(moduleInfo.getId(), moduleInfo);
    }

    private static final ModuleInfo noneInfo = new ModuleInfo();

    @Getter
    @Setter
    public static class ModuleInfo {

        private String classPath;

        private String id;

        private String groupId;

        private String path;

        private String artifactId;

        private String gitCommitHash;

        private String gitRepository;

        private String comment;

        private String version;

        public String getGitLocation() {
            String gitCommitHash = this.gitCommitHash;
            if (gitCommitHash == null || gitCommitHash.contains("$")) {
                gitCommitHash = "master";
            }
            return gitRepository + "/blob/" + gitCommitHash + "/" + path + "/";
        }

        public String getGitClassLocation(Class clazz) {
            return getGitLocation() + "src/main/java/" + (ClassUtils.getPackageName(clazz).replace(".", "/"))
                    + "/" + clazz.getSimpleName() + ".java";
        }

        public String getGitClassLocation(Class clazz, long line, long lineTo) {
            return getGitLocation() + "src/main/java/" + (ClassUtils.getPackageName(clazz).replace(".", "/"))
                    + "/" + clazz.getSimpleName() + ".java#L" + line + "-" + "L" + lineTo;
        }

        public String getId() {
            if (StringUtils.isEmpty(id)) {
                id = groupId + "/" + artifactId;
            }
            return id;
        }

        public boolean isNone() {
            return StringUtils.isEmpty(classPath);
        }
    }
}
