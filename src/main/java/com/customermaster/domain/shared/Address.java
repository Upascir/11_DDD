package com.customermaster.domain.shared;

import java.util.Objects;

/**
 * 住所
 * 
 * 住所情報を表現する値オブジェクト
 */
public class Address extends ValueObject {
    
    private final String postalCode;
    private final String prefecture;
    private final String city;
    private final String streetAddress;
    private final String building;
    
    /**
     * コンストラクタ
     * 
     * @param postalCode 郵便番号
     * @param prefecture 都道府県
     * @param city 市区町村
     * @param streetAddress 町域・番地
     * @param building 建物名（任意）
     */
    public Address(String postalCode, String prefecture, String city, 
                   String streetAddress, String building) {
        this.postalCode = validateAndTrim(postalCode, "郵便番号");
        this.prefecture = validateAndTrim(prefecture, "都道府県");
        this.city = validateAndTrim(city, "市区町村");
        this.streetAddress = validateAndTrim(streetAddress, "町域・番地");
        this.building = building != null ? building.trim() : null;
        
        validatePostalCode(this.postalCode);
    }
    
    /**
     * 必須項目のバリデーション
     */
    private String validateAndTrim(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "は必須です");
        }
        return value.trim();
    }
    
    /**
     * 郵便番号の形式チェック
     */
    private void validatePostalCode(String postalCode) {
        if (!postalCode.matches("\\d{3}-\\d{4}")) {
            throw new IllegalArgumentException("郵便番号は「123-4567」の形式で入力してください");
        }
    }
    
    /**
     * 郵便番号を取得
     */
    public String getPostalCode() {
        return postalCode;
    }
    
    /**
     * 都道府県を取得
     */
    public String getPrefecture() {
        return prefecture;
    }
    
    /**
     * 市区町村を取得
     */
    public String getCity() {
        return city;
    }
    
    /**
     * 町域・番地を取得
     */
    public String getStreetAddress() {
        return streetAddress;
    }
    
    /**
     * 建物名を取得
     */
    public String getBuilding() {
        return building;
    }
    
    /**
     * 完全な住所文字列を取得
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append("〒").append(postalCode).append(" ");
        sb.append(prefecture).append(city).append(streetAddress);
        if (building != null && !building.isEmpty()) {
            sb.append(" ").append(building);
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Address address = (Address) other;
        return Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(prefecture, address.prefecture) &&
               Objects.equals(city, address.city) &&
               Objects.equals(streetAddress, address.streetAddress) &&
               Objects.equals(building, address.building);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(postalCode, prefecture, city, streetAddress, building);
    }
    
    @Override
    public String toString() {
        return "Address{" +
               "postalCode='" + postalCode + '\'' +
               ", prefecture='" + prefecture + '\'' +
               ", city='" + city + '\'' +
               ", streetAddress='" + streetAddress + '\'' +
               ", building='" + building + '\'' +
               '}';
    }
}