package com.customermaster.domain.customer;

import com.customermaster.domain.shared.ValueObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 顧客基本情報
 * 
 * 顧客の基本的な企業情報を管理する値オブジェクト
 */
public class CustomerBasicInfo extends ValueObject {
    
    private final CustomerName customerName;
    private final String customerNameKana;
    private final String industryClassification;
    private final LocalDate establishedDate;
    private final Integer employeeCount;
    private final BigDecimal capital;
    private final BigDecimal annualRevenue;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private CustomerBasicInfo() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param customerName 顧客名（必須）
     * @param customerNameKana 顧客名カナ（必須）
     * @param industryClassification 業界分類（必須）
     * @param establishedDate 設立年月日（任意）
     * @param employeeCount 従業員数（任意）
     * @param capital 資本金（任意）
     * @param annualRevenue 年商（任意）
     */
    private CustomerBasicInfo(CustomerName customerName, String customerNameKana, 
                             String industryClassification, LocalDate establishedDate,
                             Integer employeeCount, BigDecimal capital, BigDecimal annualRevenue) {
        this.customerName = Objects.requireNonNull(customerName, "顧客名は必須です");
        this.customerNameKana = validateRequired(customerNameKana, "顧客名カナ");
        this.industryClassification = validateRequired(industryClassification, "業界分類");
        this.establishedDate = establishedDate;
        this.employeeCount = validateEmployeeCount(employeeCount);
        this.capital = validateAmount(capital, "資本金");
        this.annualRevenue = validateAmount(annualRevenue, "年商");
    }
    
    /**
     * 必須項目のバリデーション
     */
    private String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "は必須です");
        }
        return value.trim();
    }
    
    /**
     * 従業員数のバリデーション
     */
    private Integer validateEmployeeCount(Integer employeeCount) {
        if (employeeCount != null && employeeCount < 0) {
            throw new IllegalArgumentException("従業員数は0以上で入力してください");
        }
        return employeeCount;
    }
    
    /**
     * 金額のバリデーション
     */
    private BigDecimal validateAmount(BigDecimal amount, String fieldName) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + "は0以上で入力してください");
        }
        return amount;
    }
    
    /**
     * 顧客基本情報を作成
     * 
     * @param customerName 顧客名
     * @param customerNameKana 顧客名カナ
     * @param industryClassification 業界分類
     * @param establishedDate 設立年月日
     * @param employeeCount 従業員数
     * @param capital 資本金
     * @param annualRevenue 年商
     * @return CustomerBasicInfo
     */
    public static CustomerBasicInfo of(CustomerName customerName, String customerNameKana,
                                      String industryClassification, LocalDate establishedDate,
                                      Integer employeeCount, BigDecimal capital, BigDecimal annualRevenue) {
        return new CustomerBasicInfo(customerName, customerNameKana, industryClassification,
                                   establishedDate, employeeCount, capital, annualRevenue);
    }
    
    /**
     * 顧客基本情報を作成（必須項目のみ）
     * 
     * @param customerName 顧客名
     * @param customerNameKana 顧客名カナ
     * @param industryClassification 業界分類
     * @return CustomerBasicInfo
     */
    public static CustomerBasicInfo of(CustomerName customerName, String customerNameKana,
                                      String industryClassification) {
        return new CustomerBasicInfo(customerName, customerNameKana, industryClassification,
                                   null, null, null, null);
    }
    
    // Getters
    public CustomerName getCustomerName() { return customerName; }
    public String getCustomerNameKana() { return customerNameKana; }
    public String getIndustryClassification() { return industryClassification; }
    public LocalDate getEstablishedDate() { return establishedDate; }
    public Integer getEmployeeCount() { return employeeCount; }
    public BigDecimal getCapital() { return capital; }
    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    
    /**
     * 設立年月日が設定されているかチェック
     */
    public boolean hasEstablishedDate() {
        return establishedDate != null;
    }
    
    /**
     * 従業員数が設定されているかチェック
     */
    public boolean hasEmployeeCount() {
        return employeeCount != null;
    }
    
    /**
     * 資本金が設定されているかチェック
     */
    public boolean hasCapital() {
        return capital != null;
    }
    
    /**
     * 年商が設定されているかチェック
     */
    public boolean hasAnnualRevenue() {
        return annualRevenue != null;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CustomerBasicInfo that = (CustomerBasicInfo) other;
        return Objects.equals(customerName, that.customerName) &&
               Objects.equals(customerNameKana, that.customerNameKana) &&
               Objects.equals(industryClassification, that.industryClassification) &&
               Objects.equals(establishedDate, that.establishedDate) &&
               Objects.equals(employeeCount, that.employeeCount) &&
               Objects.equals(capital, that.capital) &&
               Objects.equals(annualRevenue, that.annualRevenue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerName, customerNameKana, industryClassification,
                          establishedDate, employeeCount, capital, annualRevenue);
    }
    
    @Override
    public String toString() {
        return "CustomerBasicInfo{" +
               "customerName=" + customerName +
               ", customerNameKana='" + customerNameKana + '\'' +
               ", industryClassification='" + industryClassification + '\'' +
               ", establishedDate=" + establishedDate +
               ", employeeCount=" + employeeCount +
               ", capital=" + capital +
               ", annualRevenue=" + annualRevenue +
               '}';
    }
}