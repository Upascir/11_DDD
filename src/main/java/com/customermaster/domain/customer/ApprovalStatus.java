package com.customermaster.domain.customer;

/**
 * 承認状態
 * 
 * 顧客変更申請の承認状態を表す列挙型
 */
public enum ApprovalStatus {
    
    /**
     * 承認待ち
     * 申請が提出され、部長の承認を待っている状態
     */
    PENDING("承認待ち"),
    
    /**
     * 承認済み
     * 部長によって承認され、変更が適用される状態
     */
    APPROVED("承認済み"),
    
    /**
     * 却下
     * 部長によって却下された状態
     */
    REJECTED("却下"),
    
    /**
     * 期限切れ
     * 承認期限を過ぎて自動的に却下された状態
     */
    EXPIRED("期限切れ");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    ApprovalStatus(String displayName) {
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
     * @return 承認待ちの場合true
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 承認済み状態かどうかを判定
     * 
     * @return 承認済みの場合true
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 却下状態かどうかを判定
     * 
     * @return 却下の場合true
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    /**
     * 期限切れ状態かどうかを判定
     * 
     * @return 期限切れの場合true
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }
    
    /**
     * 最終状態（承認済み、却下、期限切れ）かどうかを判定
     * 
     * @return 最終状態の場合true
     */
    public boolean isFinal() {
        return this != PENDING;
    }
}