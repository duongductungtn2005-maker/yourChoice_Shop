package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.CoAo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoAoRepository extends JpaRepository<CoAo, Integer> {}