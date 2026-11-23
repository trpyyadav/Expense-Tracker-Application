package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.response.*;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final ReportService reportService;


    public byte[] exportDateRangeReport(
            LocalDate startDate, LocalDate endDate) {

        FinancialReportResponseDTO report =
                reportService.generateFinancialReport(
                        startDate, endDate
                );

        return generateExcel(
                report.getFinancialOverview(),
                report.getCategoryExpense(),
                report.getTransactions(),
                null
        );
    }

    public byte[] exportMonthlyReport(Integer month, Integer year) {

        MonthlyReportResponseDTO report =
                reportService.getMonthlySummary(month, year);

        return generateExcel(
                report.getFinancialOverview(),
                report.getCategoryExpenses(),
                report.getTransactions(),
                report.getBudgetUsage()
        );
    }

    private byte[] generateExcel(
            FinancialOverviewResponseDTO overview,
            java.util.List<CategoryExpenseResponseDTO> categories,
            java.util.List<TransactionResponseDTO> transactions,
            java.util.List<BudgetUsageResponseDTO> budgetUsage) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            createOverviewSheet(workbook, overview, headerStyle);
            createCategorySheet(workbook, categories, headerStyle);
            createTransactionSheet(workbook, transactions, headerStyle);

            if (budgetUsage != null) {
                createBudgetSheet(workbook, budgetUsage, headerStyle);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to generate Excel report", exception
            );
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);

        style.setFont(font);
        style.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }


    private void createOverviewSheet(
            Workbook workbook,
            FinancialOverviewResponseDTO overview,
            CellStyle headerStyle) {

        Sheet sheet = workbook.createSheet("Financial Overview");

        Row header = sheet.createRow(0);

        String[] headers = {"Metric", "Amount"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String[] metrics = {
                "Total Income",
                "Total Expenses",
                "Net Savings"
        };

        BigDecimal[] amounts = {
                overview.getTotalIncome(),
                overview.getTotalExpenses(),
                overview.getNetSavings()
        };

        for (int i = 0; i < metrics.length; i++) {

            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(metrics[i]);

            row.createCell(1).setCellValue(
                    amounts[i] != null ? amounts[i].doubleValue() : 0
            );
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }


    private void createCategorySheet(
            Workbook workbook,
            java.util.List<CategoryExpenseResponseDTO> categories,
            CellStyle headerStyle) {

        Sheet sheet = workbook.createSheet("Category Expenses");

        Row header = sheet.createRow(0);

        String[] headers = {
                "Category ID",
                "Category Name",
                "Total Spent",
                "Percentage"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;

        for (CategoryExpenseResponseDTO category : categories) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(category.getCategoryId());
            row.createCell(1).setCellValue(category.getCategoryName());

            row.createCell(2).setCellValue(
                    category.getTotalSpent().doubleValue()
            );

            row.createCell(3).setCellValue(
                    category.getPercentage().doubleValue()
            );
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    private void createTransactionSheet(
            Workbook workbook,
            java.util.List<TransactionResponseDTO> transactions,
            CellStyle headerStyle) {

        Sheet sheet = workbook.createSheet("Transactions");

        Row header = sheet.createRow(0);

        String[] headers = {
                "Transaction ID",
                "Amount",
                "Type",
                "Category",
                "Description",
                "Transaction Date"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;

        for (TransactionResponseDTO transaction : transactions) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(transaction.getId());

            row.createCell(1).setCellValue(
                    transaction.getAmount().doubleValue()
            );

            row.createCell(2).setCellValue(
                    transaction.getType().toString()
            );

            row.createCell(3).setCellValue(
                    transaction.getCategoryName()
            );

            row.createCell(4).setCellValue(
                    transaction.getDescription() != null
                            ? transaction.getDescription()
                            : ""
            );

            row.createCell(5).setCellValue(
                    transaction.getTransactionDate().toString()
            );
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }



    private void createBudgetSheet(
            Workbook workbook,
            java.util.List<BudgetUsageResponseDTO> budgetUsage,
            CellStyle headerStyle) {

        Sheet sheet = workbook.createSheet("Budget Usage");

        Row header = sheet.createRow(0);

        String[] headers = {
                "Budget ID",
                "Category",
                "Budget Amount",
                "Total Spent",
                "Remaining Amount",
                "Percentage Used",
                "Exceeded"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;

        for (BudgetUsageResponseDTO budget : budgetUsage) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(budget.getBudgetId());

            row.createCell(1).setCellValue(
                    budget.getCategoryName()
            );

            row.createCell(2).setCellValue(
                    budget.getBudgetAmount().doubleValue()
            );

            row.createCell(3).setCellValue(
                    budget.getTotalSpent().doubleValue()
            );

            row.createCell(4).setCellValue(
                    budget.getRemainingAmount().doubleValue()
            );

            row.createCell(5).setCellValue(
                    budget.getPercentageUsed().doubleValue()
            );

            row.createCell(6).setCellValue(
                    budget.isExceeded()
            );
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }



}

