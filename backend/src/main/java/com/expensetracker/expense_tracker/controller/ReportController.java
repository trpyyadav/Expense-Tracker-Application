package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.response.FinancialReportResponseDTO;
import com.expensetracker.expense_tracker.dto.response.MonthlyReportResponseDTO;
import com.expensetracker.expense_tracker.service.ExcelExportService;
import com.expensetracker.expense_tracker.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;

    public ReportController(ReportService reportService, ExcelExportService excelExportService){
        this.reportService = reportService;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public ResponseEntity<FinancialReportResponseDTO> getFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){

        FinancialReportResponseDTO financialReport = reportService.generateFinancialReport(
                startDate,
                endDate
        );

        return ResponseEntity.ok(financialReport);

    }


    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponseDTO> generateMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year
    ){

        MonthlyReportResponseDTO response = reportService.getMonthlySummary(month, year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportDateRangeExcel(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        byte[] excelFile = excelExportService.exportDateRangeReport(
                        startDate,
                        endDate
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=financial_report.xlsx"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }

    @GetMapping("/monthly/export/excel")
    public ResponseEntity<byte[]> exportMonthlyExcel(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        byte[] excelFile = excelExportService.exportMonthlyReport(month, year);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=monthly_report.xlsx"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }

}
