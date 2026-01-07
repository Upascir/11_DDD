package com.customermaster.domain.customer;

/**
 * 法人ステータス
 * 
 * 法人の承認状態を表現する列挙型
 */
public enum CustomerStatus {
    
    /**
     * 下書き
     * 新規登録時の初期状態
     */
    DRAFT("下書き"),
    
    /**
     * 承認待ち
     * 営業担当者が登録・更新申請を行った状態
     */
    PENDING_APPROVAL("承認待ち"),
    
    /**
     * 承認済み
     * 部長が承認した状態（通常の運用状態）
     */
    APPROVED("承認済み"),
    
    /**
     * 却下
     * 部長が却下した状態
     */
    REJECTED("却下"),
    
    /**
     * 無効
     * 取引終了などで無効化された状態
     */
    INACTIVE("無効");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    CustomerStatus(String displayName) {
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
     * 承認待ち状態かどうかを判定
     * 
     * @return 承認待ち状態の場合true
     */
    public boolean isPendingApproval() {
        return this == PENDING_APPROVAL;
    }
    
    /**
     * 承認済み状態かどうかを判定
     * 
     * @return 承認済み状態の場合true
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 却下状態かどうかを判定
     * 
     * @return 却下状態の場合true
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    /**
     * 編集可能な状態かどうかを判定
     * 
     * @return 編集可能な場合true（下書き、却下状態）
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
    
    /**
     * アクティブな状態かどうかを判定
     * 
     * @return アクティブな場合true（無効以外）
     */
    public boolean isActive() {
        return this != INACTIVE;
    }
    
    /**
     * 承認申請可能な状態かどうかを判定
     * 
     * @return 承認申請可能な場合true（下書き、却下状態）
     */
    public boolean canRequestApproval() {
        return this == DRAFT || this == REJECTED;
    }
    
    /**
     * 承認可能な状態かどうかを判定
     * 
     * @return 承認可能な場合true（承認待ち状態）
     */
    public boolean canBeApproved() {
        return this == PENDING_APPROVAL;
    }
    
    /**
     * 却下可能な状態かどうかを判定
     * 
     * @return 却下可能な場合true（承認待ち状態）
     */
    public boolean canBeRejected() {
        return this == PENDING_APPROVAL;
    }
}