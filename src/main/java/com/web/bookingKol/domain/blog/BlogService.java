package com.web.bookingKol.domain.blog;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.blog.dtos.BlogCreateDTO;
import com.web.bookingKol.domain.blog.dtos.BlogDTO;
import com.web.bookingKol.domain.blog.dtos.BlogUpdateDTO;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BlogService {
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private BlogMapperV2 blogMapperV2;
    @Autowired
    private FileService fileService;

    public ApiResponse<BlogDTO> createBlog(UUID userId, BlogCreateDTO blogCreateDTO) {
        Blog newBlog = new Blog();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user ID: " + userId));
        newBlog.setUser(user);
        if (blogCreateDTO.getAuthor() != null && !blogCreateDTO.getAuthor().isEmpty()) {
            newBlog.setAuthor(blogCreateDTO.getAuthor());
        } else {
            newBlog.setAuthor(user.getFullName());
        }
        newBlog.setTitle(blogCreateDTO.getTitle());
        newBlog.setContent(blogCreateDTO.getContent());
        newBlog.setIsPublish(blogCreateDTO.getIsPublish());
        newBlog.setCreatedAt(Instant.now());
        blogRepository.save(newBlog);
        return ApiResponse.<BlogDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo bài viết thành công!"))
                .data(blogMapper.toDto(newBlog))
                .build();
    }

    public ApiResponse<Page<BlogDTO>> getAllBlog(Pageable pageable, boolean isAdmin) {
        Page<Blog> blogPage;
        if (!isAdmin) {
            blogPage = blogRepository.findAllDESC(pageable);
        } else {
            blogPage = blogRepository.findAllDESCAdmin(pageable);
        }
        Page<BlogDTO> blogDTOPage = blogPage.map(blogMapperV2::toDtoWithoutContent);
        return ApiResponse.<Page<BlogDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách bài viết thành công!"))
                .data(blogDTOPage)
                .build();
    }

    public ApiResponse<BlogDTO> getDetailBlog(Integer blogId, Boolean isAdmin) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId));
        if (!isAdmin && !blog.getIsPublish()) {
            throw new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId);
        }
        return ApiResponse.<BlogDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết bài viết thành công!"))
                .data(blogMapper.toDto(blog))
                .build();
    }

    public ApiResponse<BlogDTO> updateBlog(Integer blogId, BlogUpdateDTO blogUpdateDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId));
        if (blogUpdateDTO.getAuthor() != null && !blogUpdateDTO.getAuthor().isEmpty()) {
            blog.setAuthor(blogUpdateDTO.getAuthor());
        }
        if (blogUpdateDTO.getTitle() != null && !blogUpdateDTO.getTitle().isEmpty()) {
            blog.setTitle(blogUpdateDTO.getTitle());
        }
        if (blogUpdateDTO.getContent() != null && !blogUpdateDTO.getContent().isEmpty()) {
            blog.setContent(blogUpdateDTO.getContent());
        }
        if (blogUpdateDTO.getIsPublish() != null) {
            blog.setIsPublish(blogUpdateDTO.getIsPublish());
        }
        blog.setUpdatedAt(Instant.now());
        blogRepository.save(blog);
        return ApiResponse.<BlogDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Thay đổi bài viết thành công!"))
                .data(blogMapper.toDto(blog))
                .build();
    }

    public ApiResponse<?> deleteBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId));
        blogRepository.delete(blog);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa bài viết thành công!"))
                .build();
    }

    public ApiResponse<?> addThumbnailForBlog(UUID adminId, Integer blogId, MultipartFile thumbnail) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId));
        FileDTO fileDTO = fileService.uploadFilePoint(adminId, thumbnail);
        blog.setThumbnail(fileDTO.getFileUrl());
        blogRepository.save(blog);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Thêm ảnh thumbnail cho bài viết thành công"))
                .data(fileDTO)
                .build();
    }

    public ApiResponse<?> deleteThumbnailForBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết ID: " + blogId));
        blog.setThumbnail(null);
        blogRepository.save(blog);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa ảnh thumbnail cho bài viết thành công blogId: " + blogId))
                .data(null)
                .build();
    }
}
