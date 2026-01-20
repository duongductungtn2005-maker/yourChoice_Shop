package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseStatusEntity extends PrimaryEntity {

    @Column(name = "ngay_tao", updatable = false)
    @CreatedDate
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    @LastModifiedDate
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat")
    private String nguoiCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;
}