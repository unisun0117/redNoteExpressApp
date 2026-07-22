package cn.qdm.tob.modules.system.privacy.service;

import cn.qdm.tob.framework.util.AssertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 隐私文档附件本地存储。
 * <p>
 * 上传文件落盘到 {@code app.privacy.upload-dir}（默认 {@code uploads/privacy}），
 * 返回可回查的存储引用（作为 {@code file_url} 落库），下载时按引用读回。
 * </p>
 */
@Component
public class PrivacyFileStorage {

    @Value("${app.privacy.upload-dir:uploads/privacy}")
    private String uploadDir;

    /** 存储上传文件，返回存储引用（唯一文件名） */
    public String store(MultipartFile file) {
        AssertUtils.isFalse(file == null || file.isEmpty(), "上传文件不能为空");

        String original = StringUtils.defaultIfBlank(file.getOriginalFilename(), "file");
        String sanitized = sanitize(original);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + sanitized;

        Path dir = baseDir();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName).normalize();
            AssertUtils.isTrue(target.startsWith(dir), "非法文件路径");
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw AssertUtils.error("文件存储失败: {0}", e.getMessage()).get();
        }
        return storedName;
    }

    /** 按存储引用读回文件资源 */
    public Resource loadAsResource(String storedRef) {
        AssertUtils.notBlank(storedRef, "文件不存在");
        Path target = baseDir().resolve(storedRef).normalize();
        AssertUtils.isTrue(target.startsWith(baseDir()), "非法文件路径");
        Resource resource = new FileSystemResource(target);
        AssertUtils.isTrue(resource.exists() && resource.isReadable(), "文件不存在或不可读");
        return resource;
    }

    /** 从存储引用还原原始文件名（用于下载时的 Content-Disposition） */
    public String originalName(String storedRef) {
        if (StringUtils.isBlank(storedRef)) {
            return "file";
        }
        int idx = storedRef.indexOf('_');
        return idx >= 0 && idx < storedRef.length() - 1 ? storedRef.substring(idx + 1) : storedRef;
    }

    private Path baseDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
