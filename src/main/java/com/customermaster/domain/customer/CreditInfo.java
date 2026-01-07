package com.customermaster.domain.customer;

import com.customermaster.domain.shared.ValueObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 信用情報
 * 
 * 法人の信用度や財務情報を管理する値オブジェクト
 */
public class CreditInfo extends ValueObject {
    
    /**
     * 信用度ランク
     */
    public enum CreditRank {
        AAA("AAA", "最優良"),
        AA("AA", "優良"),
        A("A", "良好"),
        BBB("BBB", "普通"),
        BB("BB", "注意"),
        B("B", "要注意"),
        CCC("CCC", "危険"),
        CC("CC", "非常に危険"),
        C("C", "破綻懸念"),
        D("D", "破綻・延滞");
        
        private final String code;
        private final String displayName;
        
        CreditRank(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
    }
    
    private final CreditRank creditRank;
    private final BigDecimal creditLimit;
    private final String financialSummary;
    private final LocalDateTime lastUpdatedAt;
    private final boolean isManualEntry;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private CreditInfo() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param creditRank 信用度ランク（必須）
     * @param creditLimit 信用限度額（任意）
     * @param financialSummary 財務概要（任意）
     * @param lastUpdatedAt 最終更新日時（必須）
     * @param isManualEntry 手動入力フラグ（必須）
     */
    private CreditInfo(CreditRank creditRank, BigDecimal creditLimit, String financialSummary,
                      LocalDateTime lastUpdatedAt, boolean isManualEntry) {
        this.creditRank = Objects.requireNonNull(creditRank, "信用度ランクは必須です");
        this.creditLimit = validateCreditLimit(creditLimit);
        this.financialSummary = financialSummary != null ? financialSummary.trim() : null;
        this.lastUpdatedAt = Objects.requireNonNull(lastUpdatedAt, "最終更新日時は必須です");
        this.isManualEntry = isManualEntry;
    }
    
    /**
     * 信用限度額のバリデーション
     */
    private BigDecimal validateCreditLimit(BigDecimal creditLimit) {
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("信用限度額は0以上で入力してください");
        }
        return creditLimit;
    }
    
    /**
     * 信用情報を作成（手動入力）
     * 
     * @param creditRank 信用度ランク
     * @param creditLimit 信用限度額
     * @param financialSummary 財務概要
     * @return CreditInfo
     */
    public static CreditInfo createManualEntry(CreditRank creditRank, BigDecimal creditLimit, 
                                              String financialSummary) {
        return new CreditInfo(creditRank, creditLimit, financialSummary, 
                             LocalDateTime.now(), true);
    }
    
    /**
     * 信用情報を作成（TSR連携）
     * 
     * @param creditRank 信用度ランク
     * @param creditLimit 信用限度額
     * @param financialSummary 財務概要
     * @param lastUpdatedAt 最終更新日時
     * @return CreditInfo
     */
    public static CreditInfo createFromTsr(CreditRank creditRank, BigDecimal creditLimit,
                                          String financialSummary, LocalDateTime lastUpdatedAt) {
        return new CreditInfo(creditRank, creditLimit, financialSummary, 
                             lastUpdatedAt, false);
    }
    
    /**
     * 信用情報を作成（最小構成）
     * 
     * @param creditRank 信用度ランク
     * @return CreditInfo
     */
    public static CreditInfo createMinimal(CreditRank creditRank) {
        return new CreditInfo(creditRank, null, null, LocalDateTime.now(), true);
    }
    
    // Getters
    public CreditRank getCreditRank() { return creditRank; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public String getFinancialSummary() { return financialSummary; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public boolean isManualEntry() { return isManualEntry; }
    
    /**
     * 信用限度額が設定されているかチェック
     */
    public boolean hasCreditLimit() {
        return creditLimit != null;
    }
    
    /**
     * 財務概要が設定されているかチェック
     */
    public boolean hasFinancialSummary() {
        return financialSummary != null && !financialSummary.isEmpty();
    }
    
    /**
     * 支払い信頼性を判定
     * 
     * @return 支払い信頼性（BBB以上で信頼できると判定）
     */
    public boolean isPaymentReliable() {
        return creditRank.ordinal() <= CreditRank.BBB.ordinal();
    }
    
    /**
     * TSR連携データかどうかを判定
     */
    public boolean isFromTsr() {
        return !isManualEntry;
    }
    
    /**
     * 信用情報の表示用文字列を取得
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creditRank.getDisplayName()).append("(").append(creditRank.getCode()).append(")");
        if (hasCreditLimit()) {
            sb.append(" 限度額:").append(creditLimit.toPlainString()).append("円");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CreditInfo that = (CreditInfo) other;
        return isManualEntry == that.isManualEntry &&
               creditRank == that.creditRank &&
               Objects.equals(creditLimit, that.creditLimit) &&
               Objects.equals(financialSummary, that.financialSummary) &&
               Objects.equals(lastUpdatedAt, that.lastUpdatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(creditRank, creditLimit, financialSummary, lastUpdatedAt, isManualEntry);
    }
    
    @Override
    public String toString() {
        return "CreditInfo{" +
               "creditRank=" + creditRank +
               ", creditLimit=" + creditLimit +
               ", financialSummary='" + financialSummary + '\'' +
               ", lastUpdatedAt=" + lastUpdatedAt +
               ", isManualEntry=" + isManualEntry +
               '}';
    }
}