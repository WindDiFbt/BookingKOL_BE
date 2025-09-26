package com.web.bookingKol.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoursePackageRepository extends JpaRepository<CoursePackage, UUID> {
    @Query("""
                SELECT DISTINCT cp
                FROM CoursePackage cp
                LEFT JOIN FETCH cp.fileUsages fu
                LEFT JOIN FETCH fu.file f
                WHERE cp.id = :id AND fu.targetType = :targetType
            """)
    Optional<CoursePackage> findByCoursePackageId(@Param("id") UUID id, @Param("targetType") String targetType);
}
