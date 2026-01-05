package com.customermaster.domain.customer;

import com.customermaster.domain.shared.EntityId;

/**
 * 顧客変更申請ID
 * 
 * 顧客情報変更申請を一意に識別するためのエンティティID
 */
public class CustomerChangeRequestId extends EntityId {
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private CustomerChangeRequestId() {
        super("");
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param value ID値
     */
    private CustomerChangeRequestId(String value) {
        super(value);
    }
    
    /**
     * 新しい顧客変更申請IDを生成
     * 
     * @return 新しいCustomerChangeRequestId
     */
    public static CustomerChangeRequestId generate() {
        return new CustomerChangeRequestId(generateNewId());
    }
    
    /**
     * 既存の値から顧客変更申請IDを作成
     * 
     * @param value ID値
     * @return CustomerChangeRequestId
     */
    public static CustomerChangeRequestId of(String value) {
        return new CustomerChangeRequestId(value);
    }
}