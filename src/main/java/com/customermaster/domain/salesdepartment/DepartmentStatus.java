package com.customermaster.domain.salesdepartment;

/**
 * 営業部の状態
 * 
 * 営業部のライフサイクル状態を表現する列挙型
 */
public enum DepartmentStatus {
    
    /**
     * アクティブ
     * - 通常の営業活動を行っている状態
     * - 営業担当者の追加・削除が可能
     * - 部長の任命・解任が可能
     */
    ACTIVE("アクティブ"),
    
    /**
     * 非アクティブ
     * - 一時的に営業活動を停止している状態
     * - 営業担当者の追加・削除は不可
     * - 既存メンバーの参照は可能
     */
    INACTIVE("非アクティブ"),
    
    /**
     * 削除済み
     * - 論理削除された状態
     * - 全ての操作が制限される
     * - 履歴参照のみ可能
     */
    DELETED("削除済み");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    DepartmentStatus(String displayName) {
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
     * 営業活動が可能かどうかを判定
     * 
     * @return 営業活動可能な場合true
     */
    public boolean canOperate() {
        return this == ACTIVE;
    }
    
    /**
     * メンバー管理が可能かどうかを判定
     * 
     * @return メンバー管理可能な場合true
     */
    public boolean canManageMembers() {
        return this == ACTIVE;
    }
    
    /**
     * 参照が可能かどうかを判定
     * 
     * @return 参照可能な場合true
     */
    public boolean canView() {
        return this != DELETED;
    }
}