package cn.qdm.tob.framework.util;

import org.springframework.core.io.AbstractResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhaoxiaoyun
 */
public class MultipartFileResource extends AbstractResource {
    private final MultipartFile multipartFile;

    public MultipartFileResource(MultipartFile multipartFile) {
        assert multipartFile != null;
        this.multipartFile = multipartFile;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public long contentLength() {
        return this.multipartFile.getSize();
    }

    @Override
    public String getFilename() {
        return this.multipartFile.getOriginalFilename();
    }


    @Override
    public InputStream getInputStream() throws IOException, IllegalStateException {
        return this.multipartFile.getInputStream();
    }

    @Override
    public String getDescription() {
        return "MultipartFile resource [" + this.multipartFile.getName() + "]";
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof MultipartFileResource mfr && mfr.multipartFile.equals(this.multipartFile)));
    }

    @Override
    public int hashCode() {
        return this.multipartFile.hashCode();
    }
}
