package com.customermaster.domain.shared;

import java.util.Objects;

/**
 * 銀行口座情報
 * 
 * 銀行口座の詳細情報を管理する値オブジェクト
 */
public class BankAccount extends ValueObject {
    
    private final String bankCode;
    private final String bankName;
    private final String branchCode;
    private final String branchName;
    private final AccountType accountType;
    private final String accountNumber;
    private final String accountHolderName;
    
    /**
     * 口座種別
     */
    public enum AccountType {
        ORDINARY("普通"),
        CURRENT("当座"),
        SAVINGS("貯蓄");
        
        private final String displayName;
        
        AccountType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * コンストラクタ
     * 
     * @param bankCode 銀行コード
     * @param bankName 銀行名
     * @param branchCode 支店コード
     * @param branchName 支店名
     * @param accountType 口座種別
     * @param accountNumber 口座番号
     * @param accountHolderName 口座名義人
     */
    public BankAccount(String bankCode, String bankName, String branchCode, 
                       String branchName, AccountType accountType, 
                       String accountNumber, String accountHolderName) {
        this.bankCode = validateBankCode(bankCode);
        this.bankName = validateRequired(bankName, "銀行名");
        this.branchCode = validateBranchCode(branchCode);
        this.branchName = validateRequired(branchName, "支店名");
        this.accountType = validateRequired(accountType, "口座種別");
        this.accountNumber = validateAccountNumber(accountNumber);
        this.accountHolderName = validateRequired(accountHolderName, "口座名義人");
    }
    
    /**
     * 銀行コードのバリデーション
     */
    private String validateBankCode(String bankCode) {
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("銀行コードは必須です");
        }
        
        String trimmed = bankCode.trim();
        if (!trimmed.matches("\\d{4}")) {
            throw new IllegalArgumentException("銀行コードは4桁の数字で入力してください");
        }
        
        return trimmed;
    }
    
    /**
     * 支店コードのバリデーション
     */
    private String validateBranchCode(String branchCode) {
        if (branchCode == null || branchCode.trim().isEmpty()) {
            throw new IllegalArgumentException("支店コードは必須です");
        }
        
        String trimmed = branchCode.trim();
        if (!trimmed.matches("\\d{3}")) {
            throw new IllegalArgumentException("支店コードは3桁の数字で入力してください");
        }
        
        return trimmed;
    }
    
    /**
     * 口座番号のバリデーション
     */
    private String validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("口座番号は必須です");
        }
        
        String trimmed = accountNumber.trim();
        if (!trimmed.matches("\\d{7}")) {
            throw new IllegalArgumentException("口座番号は7桁の数字で入力してください");
        }
        
        return trimmed;
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
     * 必須項目のバリデーション（非文字列）
     */
    private <T> T validateRequired(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "は必須です");
        }
        return value;
    }
    
    // Getters
    public String getBankCode() { return bankCode; }
    public String getBankName() { return bankName; }
    public String getBranchCode() { return branchCode; }
    public String getBranchName() { return branchName; }
    public AccountType getAccountType() { return accountType; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    
    /**
     * 口座情報の表示用文字列を取得
     */
    public String getDisplayString() {
        return String.format("%s %s支店 %s %s (%s)",
                bankName, branchName, accountType.getDisplayName(), 
                accountNumber, accountHolderName);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        BankAccount that = (BankAccount) other;
        return Objects.equals(bankCode, that.bankCode) &&
               Objects.equals(branchCode, that.branchCode) &&
               accountType == that.accountType &&
               Objects.equals(accountNumber, that.accountNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bankCode, branchCode, accountType, accountNumber);
    }
    
    @Override
    public String toString() {
        return "BankAccount{" +
               "bankCode='" + bankCode + '\'' +
               ", bankName='" + bankName + '\'' +
               ", branchCode='" + branchCode + '\'' +
               ", branchName='" + branchName + '\'' +
               ", accountType=" + accountType +
               ", accountNumber='" + accountNumber + '\'' +
               ", accountHolderName='" + accountHolderName + '\'' +
               '}';
    }
}