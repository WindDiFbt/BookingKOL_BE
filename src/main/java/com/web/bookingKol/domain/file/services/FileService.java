package com.web.bookingKol.domain.file.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.file.models.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface FileService {
    FileDTO uploadFilePoint(UUID uploaderId, MultipartFile file);

    File getFileUploaded(UUID uploaderId, MultipartFile file);

    ApiResponse<FileDTO> uploadOneFile(UUID uploaderId, MultipartFile file);

    ApiResponse<List<FileDTO>> uploadMultipleFiles(UUID uploaderId, List<MultipartFile> files);

    FileUsageDTO createFileUsage(File file, UUID targetId, String targetType, Boolean isCover);

    void deleteFile(List<UUID> fileIds);
}
