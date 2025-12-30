package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Name;

/**
 * 顧客名
 * 
 * 顧客の名称を表現する値オブジェクト
 */
public class CustomerName extends Name {
    
    private static final int MAX_LENGTH = 100;
    
    /**
     * コンストラクタ
     * 
     * @param value 顧客名
     */
    public CustomerName(String value) {
        super(value);
    }
    
    /**
     * 文字列からCustomerNameを作成
     * 
     * @param value 顧客名
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