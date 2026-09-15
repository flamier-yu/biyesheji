package com.biyesheji.pms.module.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.file.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 本地磁盘存储实现
 * <p>
 * 目录结构：{uploadPath}/{bizType}/yyyy/MM/dd/{uuid}.{ext}
 */
@Slf4j
@Service
public class LocalStorageServiceImpl implements StorageService {

    private static final String DEFAULT_BIZ = "common";

    @Value("${pms.upload-path}")
    private String uploadPath;

    @Override
    public String store(MultipartFile file, String bizType) {
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
        String extName = FileUtil.extName(file.getOriginalFilename());
        String fileName = IdUtil.fastSimpleUUID() + (StrUtil.isBlank(extName) ? "" : "." + extName);
        String relativePath = (StrUtil.isBlank(bizType) ? DEFAULT_BIZ : bizType)
                + "/" + datePath + "/" + fileName;

        File dest = new File(uploadPath + File.separator + relativePath);
        FileUtil.mkParentDirs(dest);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件写入失败：{}", dest.getAbsolutePath(), e);
            throw new ServiceException(ResultCode.FILE_UPLOAD_ERROR);
        }
        return relativePath;
    }

    @Override
    public byte[] read(String path) {
        File file = resolveFile(path);
        if (!file.exists()) {
            throw new ServiceException("文件不存在或已被删除");
        }
        return FileUtil.readBytes(file);
    }

    @Override
    public boolean delete(String path) {
        File file = resolveFile(path);
        return !file.exists() || FileUtil.del(file);
    }

    @Override
    public String getUrl(String path) {
        return "/file/download?path=" + path;
    }

    @Override
    public String type() {
        return "local";
    }

    /**
     * 解析为绝对路径，并防止路径穿越攻击
     */
    private File resolveFile(String path) {
        if (StrUtil.isBlank(path) || path.contains("..")) {
            throw new ServiceException("非法的文件路径");
        }
        return new File(uploadPath + File.separator + path.replace("\\", "/"));
    }
}
