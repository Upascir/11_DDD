package com.customermaster.domain.customer;

import com.customermaster.domain.shared.EntityId;

/**
 * 顧客ID
 * 
 * 顧客を一意に識別するための値オブジェクト
 */
public class CustomerId extends EntityId {
    
    /**
     * 既存のID値からCustomerIdを作成
     * 
     * @param value ID値
     */
    public CustomerId(String value) {
        super(value);
    }
    
    /**
     * 新しいCustomerIdを生成
     * 
     * @return 新しいCustomerId
     */
    public static CustomerId generate() {
        return new CustomerId(generateNewId());
    }
    
    /**
     * 文字列からCustomerIdを作成
     * 
     * @param value ID値
     * @return CustomerId
     */
    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}