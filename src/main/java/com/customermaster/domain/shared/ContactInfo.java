package com.customermaster.domain.shared;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 連絡先情報
 * 
 * 電話番号、FAX番号、メールアドレスを管理する値オブジェクト
 */
public class ContactInfo extends ValueObject {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{2,4}-\\d{2,4}-\\d{3,4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9+_.-]*@[A-Za-z0-9][A-Za-z0-9.-]*[A-Za-z0-9]\\.[A-Za-z]{2,}$");
    
    private final String phoneNumber;
    private final String faxNumber;
    private final String emailAddress;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private ContactInfo() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param phoneNumber 電話番号（必須）
     * @param faxNumber FAX番号（任意）
     * @param emailAddress メールアドレス（任意）
     */
    private ContactInfo(String phoneNumber, String faxNumber, String emailAddress) {
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.faxNumber = validateOptionalPhoneNumber(faxNumber, "FAX番号");
        this.emailAddress = validateEmailAddress(emailAddress);
    }
    
    /**
     * 電話番号のバリデーション（必須）
     */
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("電話番号は必須です");
        }
        
        String trimmed = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("電話番号は「03-1234-5678」の形式で入力してください");
        }
        
        return trimmed;
    }
    
    /**
     * 任意の電話番号のバリデーション
     */
    private String validateOptionalPhoneNumber(String phoneNumber, String fieldName) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(fieldName + "は「03-1234-5678」の形式で入力してください");
        }
        
        return trimmed;
    }
    
    /**
     * メールアドレスのバリデーション（任意）
     */
    private String validateEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = emailAddress.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません");
        }
        
        return trimmed;
    }
    
    /**
     * 連絡先情報を作成
     * 
     * @param phoneNumber 電話番号（必須）
     * @param faxNumber FAX番号（任意）
     * @param emailAddress メールアドレス（任意）
     * @return ContactInfo
     */
    public static ContactInfo of(String phoneNumber, String faxNumber, String emailAddress) {
        return new ContactInfo(phoneNumber, faxNumber, emailAddress);
    }
    
    /**
     * 連絡先情報を作成（電話番号のみ）
     * 
     * @param phoneNumber 電話番号（必須）
     * @return ContactInfo
     */
    public static ContactInfo of(String phoneNumber) {
        return new ContactInfo(phoneNumber, null, null);
    }
    
    /**
     * 電話番号を取得
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * FAX番号を取得
     */
    public String getFaxNumber() {
        return faxNumber;
    }
    
    /**
     * メールアドレスを取得
     */
    public String getEmailAddress() {
        return emailAddress;
    }
    
    /**
     * FAX番号が設定されているかチェック
     */
    public boolean hasFaxNumber() {
        return faxNumber != null && !faxNumber.isEmpty();
    }
    
    /**
     * メールアドレスが設定されているかチェック
     */
    public boolean hasEmailAddress() {
        return emailAddress != null && !emailAddress.isEmpty();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ContactInfo that = (ContactInfo) other;
        return Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(faxNumber, that.faxNumber) &&
               Objects.equals(emailAddress, that.emailAddress);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, faxNumber, emailAddress);
    }
    
    @Override
    public String toString() {
        return "ContactInfo{" +
               "phoneNumber='" + phoneNumber + '\'' +
               ", faxNumber='" + faxNumber + '\'' +
               ", emailAddress='" + emailAddress + '\'' +
               '}';
    }
}