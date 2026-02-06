package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.entity.HoaDon;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer>, JpaSpecificationExecutor<HoaDon> {
}
