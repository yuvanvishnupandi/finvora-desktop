
package org.example.utils;

import org.example.dialogs.ExportDataDialog;
import org.example.models.Budget;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CsvExportUtil {

    private CsvExportUtil(){}

    public static void exportAll(User user, ExportDataDialog.ExportOptions opt, Path dir) throws IOException {
        Files.createDirectories(dir);

        if (opt.transactions) {
            Path file = dir.resolve("transactions.csv");
            writeTransactionsCsv(user, opt.start, opt.end, file);
        }
        if (opt.categories) {
            Path file = dir.resolve("categories.csv");
            writeCategoriesCsv(user, file);
        }
        if (opt.budgets) {
            Path file = dir.resolve("budgets.csv");
            writeBudgetsCsv(user, file);
        }
    }

    private static void writeTransactionsCsv(User user, LocalDate start, LocalDate end, Path file) throws IOException {
        List<Integer> years = SqlUtil.getAllDistinctYears(user.getId());
        List<Transaction> all = new ArrayList<>();
        for (Integer y : years) {
            List<Transaction> ylist = SqlUtil.getAllTransactionsByUserId(user.getId(), y, null);
            if (ylist != null) all.addAll(ylist);
        }
        all.sort(Comparator.comparing(Transaction::getTransactionDate));

        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            bw.write("date,category,type,amount,name\n");
            for (Transaction t : all) {
                LocalDate d = t.getTransactionDate();
                if (start != null && d.isBefore(start)) continue;
                if (end != null && d.isAfter(end)) continue;

                String category = t.getTransactionCategory() == null ? "" :
                        safe(t.getTransactionCategory().getCategoryName());
                String type = safe(t.getTransactionType());
                String name = safe(t.getTransactionName());
                String line = String.join(",",
                        d.toString(),
                        category,
                        type,
                        String.valueOf(t.getTransactionAmount()),
                        name
                );
                bw.write(line); bw.write("\n");
            }
        }
    }

    private static void writeCategoriesCsv(User user, Path file) throws IOException {
        List<TransactionCategory> cats = SqlUtil.getAllTransactionCategoriesByUser(user);
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            bw.write("id,name,color\n");
            if (cats != null) {
                for (TransactionCategory c : cats) {
                    bw.write(String.join(",",
                            String.valueOf(c.getId()),
                            safe(c.getCategoryName()),
                            safe(c.getCategoryColor())
                    ));
                    bw.write("\n");
                }
            }
        }
    }

    private static void writeBudgetsCsv(User user, Path file) throws IOException {
        List<Budget> budgets = BudgetStore.getBudgets(user.getId());
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            bw.write("id,category,periodType,year,month,quarter,limit,spent,remaining\n");
            if (budgets != null) {
                for (Budget b : budgets) {
                    
                    b.setSpentAmount(SpentCalculator.compute(user, b));
                    bw.write(String.join(",",
                            String.valueOf(b.getId() == null ? 0 : b.getId()),
                            safe(b.getCategory()),
                            b.getPeriodType() == null ? "" : b.getPeriodType().name(),
                            String.valueOf(b.getYear()),
                            b.getMonth() == null ? "" : String.valueOf(b.getMonth()),
                            b.getQuarter() == null ? "" : String.valueOf(b.getQuarter()),
                            b.getLimitAmount() == null ? "0" : b.getLimitAmount().toPlainString(),
                            b.getSpentAmount() == null ? "0" : b.getSpentAmount().toPlainString(),
                            b.getRemaining() == null ? "0" : b.getRemaining().toPlainString()
                    ));
                    bw.write("\n");
                }
            }
        }
    }

    private static String safe(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v + "\"";
        return v;
    }

    private static final class SpentCalculator {
        static java.math.BigDecimal compute(User user, Budget b) {
            if (b == null || user == null) return java.math.BigDecimal.ZERO;
            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;

            switch (b.getPeriodType()) {
                case MONTHLY -> {
                    List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), b.getMonth());
                    sum = sum.add(sumCategory(tx, b.getCategory()));
                }
                case QUARTERLY -> {
                    int q = b.getQuarter() == null ? 1 : b.getQuarter();
                    int start = (q - 1) * 3 + 1;
                    for (int m = start; m <= start + 2; m++) {
                        List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), m);
                        sum = sum.add(sumCategory(tx, b.getCategory()));
                    }
                }
                case YEARLY -> {
                    List<Transaction> tx = SqlUtil.getAllTransactionsByUserId(user.getId(), b.getYear(), null);
                    sum = sum.add(sumCategory(tx, b.getCategory()));
                }
            }
            return sum.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        private static java.math.BigDecimal sumCategory(List<Transaction> tx, String categoryName) {
            java.math.BigDecimal s = java.math.BigDecimal.ZERO;
            if (tx == null || categoryName == null) return s;
            String wanted = categoryName.trim();
            for (Transaction t : tx) {
                if (!"expense".equalsIgnoreCase(t.getTransactionType())) continue;
                if (t.getTransactionCategory() == null) continue;
                String cat = t.getTransactionCategory().getCategoryName();
                if (cat != null && cat.trim().equalsIgnoreCase(wanted)) {
                    s = s.add(java.math.BigDecimal.valueOf(t.getTransactionAmount()));
                }
            }
            return s;
        }
    }
}