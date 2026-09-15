package com.biyesheji.pms.module.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.biyesheji.pms.common.enums.ResultCode;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.framework.security.SecurityUtils;
import com.biyesheji.pms.module.file.entity.PmAttachment;
import com.biyesheji.pms.module.file.mapper.PmAttachmentMapper;
import com.biyesheji.pms.module.file.service.IPmAttachmentService;
import com.biyesheji.pms.module.file.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 附件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmAttachmentServiceImpl implements IPmAttachmentService {

    /** 单文件大小上限：50MB */
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    /** 允许上传的扩展名 */
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "csv", "zip", "rar", "7z"
    ));

    private final PmAttachmentMapper attachMapper;
    private final StorageService storageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmAttachment upload(MultipartFile file, String bizType, Long bizId) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ResultCode.FILE_EMPTY);
        }
        String originalName = file.getOriginalFilename();
        String ext = StrUtil.isNotBlank(originalName)
                ? FileUtil.extName(originalName).toLowerCase() : "";
        if (StrUtil.isNotBlank(ext) && !ALLOWED_EXT.contains(ext)) {
            throw new ServiceException(ResultCode.FILE_TYPE_NOT_ALLOWED, "不支持的文件类型：" + ext);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException(ResultCode.FILE_SIZE_EXCEED);
        }

        String md5 = calcMd5(file);
        String path = storageService.store(file, bizType);

        PmAttachment attachment = new PmAttachment();
        attachment.setBizType(StrUtil.isBlank(bizType) ? "common" : bizType);
        attachment.setBizId(bizId == null ? 0L : bizId);
        attachment.setFileName(originalName);
        attachment.setFilePath(path);
        attachment.setFileSuffix(ext);
        attachment.setFileSize(file.getSize());
        attachment.setFileMd5(md5);
        attachment.setUploadBy(SecurityUtils.getUserIdOrNull());
        attachment.setCreateTime(LocalDateTime.now());
        attachMapper.insert(attachment);

        attachment.setUrl(storageService.getUrl(path));
        return attachment;
    }

    @Override
    public List<PmAttachment> listByBiz(String bizType, Long bizId) {
        List<PmAttachment> list = attachMapper.selectByBiz(bizType, bizId);
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        for (PmAttachment a : list) {
            a.setUrl(storageService.getUrl(a.getFilePath()));
            a.setSizeText(FileUtil.readableFileSize(a.getFileSize() == null ? 0L : a.getFileSize()));
        }
        return list;
    }

    @Override
    public PmAttachment getAttachment(Long attachId) {
        PmAttachment attachment = attachMapper.selectById(attachId);
        if (attachment == null) {
            throw new ServiceException("附件不存在");
        }
        return attachment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttachment(Long attachId) {
        PmAttachment attachment = attachMapper.selectById(attachId);
        if (attachment == null) {
            return false;
        }
        // 只有上传者或管理员可删除
        Long userId = SecurityUtils.getUserIdOrNull();
        if (!SecurityUtils.isAdmin(userId)
                && attachment.getUploadBy() != null
                && !attachment.getUploadBy().equals(userId)) {
            throw new ServiceException("只能删除本人上传的附件");
        }
        storageService.delete(attachment.getFilePath());
        return attachMapper.deleteById(attachId) > 0;
    }

    @Override
    public PmAttachment findByMd5(String md5) {
        return StrUtil.isBlank(md5) ? null : attachMapper.selectByMd5(md5);
    }

    /**
     * 读取流计算 MD5，用于秒传与去重判断
     */
    private String calcMd5(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return SecureUtil.md5(in);
        } catch (IOException e) {
            log.warn("计算文件 MD5 失败：{}", file.getOriginalFilename());
            return null;
        }
    }
}
