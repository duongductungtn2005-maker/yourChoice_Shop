package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.ThongBao;
import org.example.yourchoiceshop.repository.ThongBaoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ThongBaoService {

    private final ThongBaoRepository thongBaoRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void guiThongBaoDonHangMoi(String maHoaDon, String tenKhachHang, BigDecimal tongTien, String loaiHoaDon) {
        String loaiText = "GIAO_HANG".equals(loaiHoaDon) ? "Giao hàng" : "Tại quầy";
        String tienText = formatCurrency(tongTien);

        ThongBao tb = new ThongBao();
        tb.setTieuDe("Đơn hàng mới #" + maHoaDon);
        tb.setNoiDung(tenKhachHang + " đã đặt đơn " + loaiText + " - " + tienText);
        tb.setLoai("DON_HANG_MOI");
        tb.setMaHoaDon(maHoaDon);
        tb.setDaDoc(false);
        tb.setNgayTao(LocalDateTime.now());
        thongBaoRepo.save(tb);

        messagingTemplate.convertAndSend("/topic/notifications", tb);
    }

    public List<ThongBao> getRecent() {
        return thongBaoRepo.findTop50ByOrderByNgayTaoDesc();
    }

    public long countUnread() {
        return thongBaoRepo.countByDaDoc(false);
    }

    @Transactional
    public void markAsRead(Integer id) {
        ThongBao tb = thongBaoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        tb.setDaDoc(true);
        thongBaoRepo.save(tb);
    }

    @Transactional
    public void markAllAsRead() {
        thongBaoRepo.markAllAsRead();
    }

    private String formatCurrency(BigDecimal val) {
        if (val == null) return "0 đ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(val) + " đ";
    }
}
