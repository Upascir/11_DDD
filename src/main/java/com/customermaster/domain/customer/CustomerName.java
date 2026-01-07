package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Name;

/**
 * 法人名
 * 
 * 法人の名称を表現する値オブジェクト
 */
public class CustomerName extends Name {
    
    private static final int MAX_LENGTH = 100;
    
    /**
     * コンストラクタ
     * 
     * @param value 法人名
     */
    private CustomerName(String value) {
        super(value);
    }
    
    /**
     * 文字列からCustomerNameを作成
     * 
     * @param value 法人名
     * @return CustomerName
     */
    public static CustomerName of(String value) {
        return new CustomerName(value);
    }
    
    @Override
    protected int getMaxLength() {
        return MAX_LENGTH;
    }
}