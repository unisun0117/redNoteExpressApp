package cn.qdm.tob.modules.system.privacy.service;

import org.springframework.core.io.Resource;

/**
 * 文件下载载体（供 Controller 组装二进制响应，不作为 JSON 出参）。
 */
public record FileDownload(Resource resource, String fileName) {
}
