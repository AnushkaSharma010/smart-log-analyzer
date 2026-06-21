package com.anushka.log_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.anushka.log_service.entity.LogMetaData;


public interface LogMetaDataRepository extends JpaRepository<LogMetaData, Long> {
    Page<LogMetaData> findAllByOrderByUploadTimeDesc(Pageable pageable);
}
