package com.customermaster.domain.user;

/**
 * ユーザーの役割
 * 
 * システム内でのユーザーの権限レベルを表現する列挙型
 */
public enum Role {
    
    /**
     * 営業担当者
     * - 顧客データの参照・更新申請
     * - 担当顧客の管理
     */
    SALES_REPRESENTATIVE("営業担当者"),
    
    /**
     * 部長
     * - 営業担当者の権限に加えて
     * - 顧客データ変更の承認・却下
     * - 担当者変更の承認・却下
     */
    DEPARTMENT_MANAGER("部長"),
    
    /**
     * システム管理者
     * - 全システム機能へのアクセス
     * - 緊急時の強制承認
     * - ユーザー管理
     * - システム設定
     */
    SYSTEM_ADMINISTRATOR("システム管理者");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 表示名を取得
     * 
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 営業担当者かどうかを判定
     * 
     * @return 営業担当者の場合true
     */
    public boolean isSalesRepresentative() {
        return this == SALES_REPRESENTATIVE || this == DEPARTMENT_MANAGER;
    }
    
    /**
     * 部長かどうかを判定
     * 
     * @return 部長の場合true
     */
    public boolean isDepartmentManager() {
        return this == DEPARTMENT_MANAGER;
    }
    
    /**
     * システム管理者かどうかを判定
     * 
     * @return システム管理者の場合true
     */
    public boolean isSystemAdministrator() {
        return this == SYSTEM_ADMINISTRATOR;
    }
    
    /**
     * 承認権限を持つかどうかを判定
     * 
     * @return 承認権限を持つ場合true
     */
    public boolean canApprove() {
        return this == DEPARTMENT_MANAGER || this == SYSTEM_ADMINISTRATOR;
    }
    
    /**
     * 緊急承認権限を持つかどうかを判定
     * 
     * @return 緊急承認権限を持つ場合true
     */
    public boolean canForceApprove() {
        return this == SYSTEM_ADMINISTRATOR;
    }
}