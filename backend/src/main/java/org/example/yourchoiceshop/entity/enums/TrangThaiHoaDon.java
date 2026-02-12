package org.example.yourchoiceshop.entity.enums;

import lombok.Getter;

@Getter

public enum TrangThaiHoaDon {
    DA_HUY(0),
    CHO_XAC_NHAN(1),
    CHO_GIAO_HANG(2),
    DANG_VAN_CHUYEN(3),
    HOAN_THANH(4);

    private final int value;

    TrangThaiHoaDon(int value) {
        this.value = value;
    }
}

