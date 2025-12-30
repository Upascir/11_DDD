package com.customermaster.domain.user;

import com.customermaster.domain.shared.EntityId;

/**
 * ユーザーID
 * 
 * ユーザーを一意に識別するための値オブジェクト
 */
public class UserId extends EntityId {
    
    /**
     * 既存のID値からUserIdを作成
     * 
     * @param value ID値
     */
    public UserId(String value) {
        super(value);
    }
    
    /**
     * 新しいUserIdを生成
     * 
     * @return 新しいUserId
     */
    public static UserId generate() {
        return new UserId(generateNewId());
    }
    
    /**
     * 文字列からUserIdを作成
     * 
     * @param value ID値
     * @return UserId
     */
    public static UserId of(String value) {
        return new UserId(value);
    }
}