package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.EmailRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, Integer> {
    // Hàm check xem email đã tồn tại trong DB chưa để tránh lưu trùng
    boolean existsByEmail(String email);
}