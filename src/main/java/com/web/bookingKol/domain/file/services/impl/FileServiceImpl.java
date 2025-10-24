package com.web.bookingKol.domain.file.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.file.FileValidator;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.file.mappers.FileMapper;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.file.repositories.FileRepository;
import com.web.bookingKol.domain.file.repositories.FileUsageRepository;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.file.services.SupabaseStorageService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private SupabaseStorageService supabaseStorageService;
    @Autowired
    private FileValidator fileValidator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileUsageRepository fileUsageRepository;

    @Transactional
    @Override
    public FileDTO uploadFilePoint(UUID uploaderId, MultipartFile file) {
        File fileUploading = getFileUploaded(uploaderId, file);
        return fileMapper.toDto(fileUploading);
    }

    @Transactional
    @Override
    public File getFileUploaded(UUID uploaderId, MultipartFile file) {
        if (file == null || file.getSize() <= 0) {
            throw new IllegalArgumentException("File must not be null or empty");
        }
        File fileUploading = new File();
        if (fileValidator.isImage(file)) {
            fileUploading.setFileType(Enums.FileType.IMAGE.name());
        } else if (fileValidator.isVideo(file)) {
            fileUploading.setFileType(Enums.FileType.VIDEO.name());
        } else if (fileValidator.isDocument(file)) {
            fileUploading.setFileType(Enums.FileType.DOCUMENT.name());
        } else {
            throw new IllegalArgumentException("Unsupported file type");
        }
        UUID fileId = UUID.randomUUID();
        fileUploading.setId(fileId);
        User uploader = userRepository.findById(uploaderId).orElseThrow(() -> new IllegalArgumentException("Uploader not found"));
        fileUploading.setUploader(uploader);
        fileUploading.setStatus(Enums.FileStatus.ACTIVE.name());

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = extension == null || extension.isEmpty()
                ? fileId.toString()
                : fileId + "." + extension.toLowerCase();
        fileUploading.setFileName(fileName);

        String fileUrl = supabaseStorageService.uploadFile(file, fileName, file.getContentType());
        fileUploading.setFileUrl(fileUrl);

        fileUploading.setSizeBytes(file.getSize());
        fileUploading.setCreatedAt(Instant.now());
        fileRepository.save(fileUploading);
        return fileUploading;
    }

    @Transactional
    @Override
    public ApiResponse<FileDTO> uploadOneFile(UUID uploaderId, MultipartFile file) {
        return ApiResponse.<FileDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("File uploaded successfully"))
                .data(uploadFilePoint(uploaderId, file))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<List<FileDTO>> uploadMultipleFiles(UUID uploaderId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Files must not be null or empty");
        }
        List<FileDTO> fileDTOS = files.stream().map(file -> uploadFilePoint(uploaderId, file)).toList();
        return ApiResponse.<List<FileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("File uploaded successfully"))
                .data(fileDTOS)
                .build();
    }

    @Override
    public FileUsageDTO createFileUsage(File file, UUID targetId, String targetType, Boolean isCover) {
        FileUsage fileUsage = new FileUsage();
        fileUsage.setId(UUID.randomUUID());
        fileUsage.setFile(file);
        fileUsage.setTargetId(targetId);
        fileUsage.setTargetType(targetType);
        fileUsage.setIsCover(false);
        fileUsage.setIsActive(true);
        fileUsage.setCreatedAt(Instant.now());
        fileUsageRepository.save(fileUsage);
        return fileUsageMapper.toDto(fileUsage);
    }

    @Override
    public void deleteFile(List<UUID> fileIds) {
        for (UUID fileId : fileIds) {
            File file = fileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
            file.setStatus(Enums.FileStatus.DELETED.name());
            fileRepository.save(file);
        }
    }
}