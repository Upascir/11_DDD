package com.customermaster.domain.user;

import com.customermaster.domain.shared.Name;

/**
 * ユーザー名
 * 
 * ユーザーの名前を表現する値オブジェクト
 */
public class UserName extends Name {
    
    private static final int MAX_LENGTH = 50;
    
    /**
     * コンストラクタ
     * 
     * @param value ユーザー名
     */
    public UserName(String value) {
        super(value);
    }
    
    /**
     * 文字列からUserNameを作成
     * 
     * @param value ユーザー名
     * @return UserName
     */
    public static UserName of(String value) {
        return new UserName(value);
    }
    
    @Override
    protected int getMaxLength() {
        return MAX_LENGTH;
    }
}