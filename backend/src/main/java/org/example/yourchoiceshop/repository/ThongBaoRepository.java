package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    List<ThongBao> findTop50ByOrderByNgayTaoDesc();

    long countByDaDoc(Boolean daDoc);

    @Modifying
    @Query("UPDATE ThongBao t SET t.daDoc = true WHERE t.daDoc = false")
    void markAllAsRead();
}
