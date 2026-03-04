package org.example.yourchoiceshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendMailRequest {
    // Danh sách các địa chỉ email khách hàng được chọn từ Frontend
    private List<String> emails;
}