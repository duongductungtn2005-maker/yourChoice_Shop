package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ManualReportRequest {
    private List<String> emails;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}