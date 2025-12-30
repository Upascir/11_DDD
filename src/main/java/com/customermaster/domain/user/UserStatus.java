package com.customermaster.domain.user;

/**
 * ユーザーの状態
 * 
 * ユーザーアカウントの現在の状態を表現する列挙型
 */
public enum UserStatus {
    
    /**
     * アクティブ
     * - 通常の利用可能状態
     * - ログイン・システム利用が可能
     */
    ACTIVE("アクティブ"),
    
    /**
     * 無効
     * - アカウントが無効化された状態
     * - ログイン不可
     * - 管理者による手動復旧が必要
     */
    INACTIVE("無効"),
    
    /**
     * ロック中
     * - ログイン失敗によりアカウントがロックされた状態
     * - 一定時間経過後に自動解除
     * - または管理者による手動解除
     */
    LOCKED("ロック中"),
    
    /**
     * 削除済み
     * - 論理削除された状態
     * - システムから除外されているが履歴は保持
     */
    DELETED("削除済み");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    UserStatus(String displayName) {
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
     * ログイン可能かどうかを判定
     * 
     * @return ログイン可能な場合true
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    /**
     * システム利用可能かどうかを判定
     * 
     * @return システム利用可能な場合true
     */
    public boolean canUseSystem() {
        return this == ACTIVE;
    }
    
    /**
     * 一時的な状態かどうかを判定
     * 
     * @return 一時的な状態の場合true（自動復旧可能）
     */
    public boolean isTemporary() {
        return this == LOCKED;
    }
}