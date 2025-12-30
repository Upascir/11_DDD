package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.shared.Name;

/**
 * 部署名
 * 
 * 営業部の名称を表現する値オブジェクト
 */
public class DepartmentName extends Name {
    
    private static final int MAX_LENGTH = 50;
    
    /**
     * コンストラクタ
     * 
     * @param value 部署名
     */
    public DepartmentName(String value) {
        super(value);
    }
    
    /**
     * 文字列からDepartmentNameを作成
     * 
     * @param value 部署名
     * @return DepartmentName
     */
    public static DepartmentName of(String value) {
        return new DepartmentName(value);
    }
    
    @Override
    protected int getMaxLength() {
        return MAX_LENGTH;
    }
}