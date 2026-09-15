package com.biyesheji.pms.module.file.service;

import com.biyesheji.pms.module.file.entity.PmAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件服务
 */
public interface IPmAttachmentService {

    /**
     * 上传附件并落库
     */
    PmAttachment upload(MultipartFile file, String bizType, Long bizId);

    /**
     * 查询某业务对象下的附件
     */
    List<PmAttachment> listByBiz(String bizType, Long bizId);

    /**
     * 按 ID 查询附件（含存储路径，用于下载）
     */
    PmAttachment getAttachment(Long attachId);

    /**
     * 删除附件（同时清理物理文件）
     */
    boolean deleteAttachment(Long attachId);

    /**
     * 判断是否存在同 MD5 文件（秒传）
     */
    PmAttachment findByMd5(String md5);
}
