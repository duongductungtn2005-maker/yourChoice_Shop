package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.TayAo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TayAoRepository extends JpaRepository<TayAo, Integer> {}