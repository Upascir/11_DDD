package com.customermaster.domain.assign;

/**
 * 担当者変更の種類
 * 
 * 営業部内変更と営業部間変更を区別する列挙型
 */
public enum AssignmentChangeType {
    
    /**
     * 営業部内変更
     * 同じ営業部内での担当者変更
     */
    WITHIN_DEPARTMENT("営業部内変更"),
    
    /**
     * 営業部間変更
     * 異なる営業部への担当者変更
     */
    CROSS_DEPARTMENT("営業部間変更");
    
    private final String displayName;
    
    AssignmentChangeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 営業部内変更かどうかを判定
     * 
     * @return 営業部内変更の場合true
     */
    public boolean isWithinDepartment() {
        return this == WITHIN_DEPARTMENT;
    }
    
    /**
     * 営業部間変更かどうかを判定
     * 
     * @return 営業部間変更の場合true
     */
    public boolean isCrossDepartment() {
        return this == CROSS_DEPARTMENT;
    }
}