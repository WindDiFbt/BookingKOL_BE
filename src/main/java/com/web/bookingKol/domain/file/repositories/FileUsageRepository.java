package com.web.bookingKol.domain.file.repositories;

import com.web.bookingKol.domain.file.models.FileUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileUsageRepository extends JpaRepository<FileUsage, UUID> {
    @Query("""
            SELECT fu FROM FileUsage fu
            LEFT JOIN FETCH fu.file f
            WHERE f.fileType IN ('IMAGE', 'VIDEO') AND f.status = 'ACTIVE'
            AND fu.targetType = 'PORTFOLIO' AND fu.isActive = true AND fu.targetId = :kolId ORDER BY f.fileType DESC, fu.createdAt DESC
            """)
    List<FileUsage> findAllImageAndVideoActiveByKolId(@Param("kolId") UUID kolId);

//    @Query("""
//            SELECT fu FROM FileUsage fu
//            LEFT JOIN FETCH fu.file f
//            WHERE f.fileType IN ('IMAGE', 'VIDEO') AND f.status = 'ACTIVE'
//            AND fu.targetId = :kolId AND fu.isActive = true ORDER BY f.createdAt DESC
//            """)
//    List<FileUsage> findAllImageAndVideoActiveByKolId(@Param("kolId") UUID kolId);
}
