package org.example.yourchoiceshop.config;

import org.example.yourchoiceshop.entity.EmailRecipient;
import org.example.yourchoiceshop.repository.EmailRecipientRepository;
import org.example.yourchoiceshop.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatisticReportScheduler {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailRecipientRepository emailRepository; // Lấy database danh bạ ra

    // Lấy toàn bộ email trong danh bạ để gửi
    private List<String> getEmailList() {
        return emailRepository.findAll().stream()
                .map(EmailRecipient::getEmail)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 17 * * ?")
    public void sendDailyReport() {
        List<String> emails = getEmailList();
        if(emails.isEmpty()) return; // Nếu danh bạ trống thì khỏi gửi

        LocalDate today = LocalDate.now();
        String dateString = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        reportService.generateAndSendAutoReport(emails, "Báo Cáo Ngày", dateString, today.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    @Scheduled(cron = "0 0 17 L * ?")
    public void sendMonthlyReport() {
        List<String> emails = getEmailList();
        if(emails.isEmpty()) return;

        YearMonth currentMonth = YearMonth.now();
        String dateString = "Tháng " + currentMonth.getMonthValue() + "/" + currentMonth.getYear();
        
        reportService.generateAndSendAutoReport(emails, "Báo Cáo Tháng", dateString, currentMonth.atDay(1).atStartOfDay(), currentMonth.atEndOfMonth().atTime(LocalTime.MAX));
    }

    @Scheduled(cron = "0 0 17 L 3,6,9,12 ?")
    public void sendQuarterlyReport() {
        List<String> emails = getEmailList();
        if(emails.isEmpty()) return;

        LocalDate today = LocalDate.now();
        int currentQuarter = today.get(IsoFields.QUARTER_OF_YEAR);
        LocalDate startOfQuarterDate = LocalDate.of(today.getYear(), (currentQuarter - 1) * 3 + 1, 1);
        String dateString = "Quý " + currentQuarter + "/" + today.getYear();
        
        reportService.generateAndSendAutoReport(emails, "Báo Cáo Quý", dateString, startOfQuarterDate.atStartOfDay(), YearMonth.from(startOfQuarterDate.plusMonths(2)).atEndOfMonth().atTime(LocalTime.MAX));
    }

    @Scheduled(cron = "0 0 17 L 12 ?")
    public void sendYearlyReport() {
        List<String> emails = getEmailList();
        if(emails.isEmpty()) return;

        int year = LocalDate.now().getYear();
        String dateString = "Năm " + year;
        
        reportService.generateAndSendAutoReport(emails, "Báo Cáo Năm", dateString, LocalDate.of(year, 1, 1).atStartOfDay(), LocalDate.of(year, 12, 31).atTime(LocalTime.MAX));
    }
}