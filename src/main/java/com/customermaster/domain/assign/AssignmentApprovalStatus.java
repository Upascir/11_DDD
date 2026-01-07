package com.customermaster.domain.assign;

/**
 * 担当者変更申請の承認ステータス
 * 
 * 担当者変更申請の現在の状態を表す列挙型
 */
public enum AssignmentApprovalStatus {
    
    /**
     * 申請中
     * 申請が提出され、承認待ちの状態
     */
    PENDING("申請中"),
    
    /**
     * 部分承認
     * 営業部間変更で一方の営業部のみ承認済みの状態
     */
    PARTIALLY_APPROVED("部分承認"),
    
    /**
     * 承認済み
     * 必要な全ての承認が完了した状態
     */
    APPROVED("承認済み"),
    
    /**
     * 却下
     * 申請が却下された状態
     */
    REJECTED("却下"),
    
    /**
     * 期限切れ
     * 承認期限を過ぎて自動的に却下された状態
     */
    EXPIRED("期限切れ");
    
    private final String displayName;
    
    AssignmentApprovalStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 承認待ち状態かどうかを判定
     * 
     * @return 承認待ち状態の場合true
     */
    public boolean isPending() {
        return this == PENDING || this == PARTIALLY_APPROVED;
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
        return this == REJECTED || this == EXPIRED;
    }
    
    /**
     * 部分承認状態かどうかを判定
     * 
     * @return 部分承認状態の場合true
     */
    public boolean isPartiallyApproved() {
        return this == PARTIALLY_APPROVED;
    }
    
    /**
     * 編集可能な状態かどうかを判定
     * 
     * @return 編集可能な場合true
     */
    public boolean isEditable() {
        return this == PENDING || this == REJECTED;
    }
}