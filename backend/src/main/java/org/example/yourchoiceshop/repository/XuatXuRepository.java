package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {}
