package org.example.yourchoiceshop.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig.VnPayProperties vnPayProperties;

    /**
     * Tạo URL thanh toán VNPay
     * @param maHoaDon mã hóa đơn
     * @param totalAmount tổng tiền (VND, không nhân 100)
     * @param orderInfo mô tả đơn hàng
     * @param ipAddress IP của khách hàng
     * @return URL redirect sang VNPay
     */
    public String createPaymentUrl(String maHoaDon, long totalAmount, String orderInfo, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();

        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(totalAmount * 100)); // VNPay yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", maHoaDon);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cal.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cal.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cal.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Tạo query string và hash
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<Map.Entry<String, String>> itr = vnpParams.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Hash data
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnpSecureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnPayProperties.getPayUrl() + "?" + query;
    }

    /**
     * Xác thực callback/return từ VNPay
     * @param params tất cả request parameters từ VNPay trả về
     * @return true nếu chữ ký hợp lệ
     */
    public boolean validateSignature(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) return false;

        // Loại bỏ các trường hash ra khỏi params
        Map<String, String> filteredParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                filteredParams.put(key, entry.getValue());
            }
        }

        // Tạo lại hash data
        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = filteredParams.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                hashData.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    /**
     * Lấy IP từ HttpServletRequest
     */
    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Nếu có nhiều IP (proxy chain), lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC SHA512", e);
        }
    }
}
