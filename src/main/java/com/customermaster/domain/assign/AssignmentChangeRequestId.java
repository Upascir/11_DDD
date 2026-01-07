package com.customermaster.domain.assign;

import com.customermaster.domain.shared.EntityId;

/**
 * 担当者変更申請ID
 * 
 * 担当者変更申請を一意に識別するID値オブジェクト
 */
public class AssignmentChangeRequestId extends EntityId {
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private AssignmentChangeRequestId() {
        super("");
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param value ID値
     */
    private AssignmentChangeRequestId(String value) {
        super(value);
    }
    
    /**
     * 新しい担当者変更申請IDを生成
     * 
     * @return 新しいAssignmentChangeRequestId
     */
    public static AssignmentChangeRequestId generate() {
        return new AssignmentChangeRequestId(generateNewId());
    }
    
    /**
     * 既存の担当者変更申請IDから作成
     * 
     * @param value ID値
     * @return AssignmentChangeRequestId
     */
    public static AssignmentChangeRequestId of(String value) {
        return new AssignmentChangeRequestId(value);
    }
}