package com.web.bookingKol.domain.blog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    @Query("SELECT b FROM Blog b WHERE b.isPublish = true ORDER BY b.createdAt DESC")
    Page<Blog> findAllDESC(Pageable pageable);

    @Query("SELECT b FROM Blog b ORDER BY b.createdAt DESC")
    Page<Blog> findAllDESCAdmin(Pageable pageable);
}
