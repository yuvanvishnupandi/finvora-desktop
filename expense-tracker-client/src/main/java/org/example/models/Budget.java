package org.example.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Budget {

    public enum PeriodType { MONTHLY, QUARTERLY, YEARLY }

    private Long id;
    private String userEmail;
    private String category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount = BigDecimal.ZERO;
    private int year;
    private PeriodType periodType;
    private Integer month;   
    private Integer quarter; 

    public Budget() {}

    public Budget(Long id, String userEmail, String category,
                  BigDecimal limitAmount, BigDecimal spentAmount,
                  int year, PeriodType periodType, Integer month, Integer quarter) {
        this.id = id;
        this.userEmail = userEmail;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount == null ? BigDecimal.ZERO : spentAmount;
        this.year = year;
        this.periodType = periodType;
        this.month = month;
        this.quarter = quarter;
    }

    public BigDecimal getRemaining() {
        if (limitAmount == null) return BigDecimal.ZERO;
        BigDecimal spent = spentAmount == null ? BigDecimal.ZERO : spentAmount;
        BigDecimal rem = limitAmount.subtract(spent);
        return rem.signum() < 0 ? BigDecimal.ZERO : rem;
    }

    public double getProgressRatio() {
        if (limitAmount == null || limitAmount.signum() == 0) return 0.0;
        BigDecimal spent = spentAmount == null ? BigDecimal.ZERO : spentAmount;
        BigDecimal ratio = spent.divide(limitAmount, 6, java.math.RoundingMode.HALF_UP);
        double r = ratio.doubleValue();
        if (r < 0) r = 0;
        if (r > 1) r = 1;
        return r;
    }

    public String getPeriodLabel() {
        if (periodType == null) return String.valueOf(year);
        switch (periodType) {
            case MONTHLY:
                String mm = month == null ? "--" : String.format("%02d", month);
                return year + "-" + mm;
            case QUARTERLY:
                String qq = quarter == null ? "-" : ("Q" + quarter);
                return year + "-" + qq;
            default:
                return year + "-Year";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        Budget b = (Budget) o;
        return year == b.year &&
                Objects.equals(userEmail, b.userEmail) &&
                Objects.equals(category, b.category) &&
                periodType == b.periodType &&
                Objects.equals(month, b.month) &&
                Objects.equals(quarter, b.quarter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, category, year, periodType, month, quarter);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }
}