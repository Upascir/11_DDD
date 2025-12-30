package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.shared.EntityId;

/**
 * 営業部ID
 * 
 * 営業部を一意に識別するための値オブジェクト
 */
public class SalesDepartmentId extends EntityId {
    
    /**
     * 既存のID値からSalesDepartmentIdを作成
     * 
     * @param value ID値
     */
    private SalesDepartmentId(String value) {
        super(value);
    }
    
    /**
     * 新しいSalesDepartmentIdを生成
     * 
     * @return 新しいSalesDepartmentId
     */
    public static SalesDepartmentId generate() {
        return new SalesDepartmentId(generateNewId());
    }
    
    /**
     * 文字列からSalesDepartmentIdを作成
     * 
     * @param value ID値
     * @return SalesDepartmentId
     */
    public static SalesDepartmentId of(String value) {
        return new SalesDepartmentId(value);
    }
}