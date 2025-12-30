package com.customermaster.domain.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * エンティティIDの基底クラス
 * 
 * すべてのエンティティIDの共通実装を提供
 */
public abstract class EntityId extends ValueObject {
    
    private final String value;
    
    /**
     * コンストラクタ
     * 
     * @param value ID値
     * @throws IllegalArgumentException ID値がnullまたは空の場合
     */
    protected EntityId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ID値は必須です");
        }
        this.value = value.trim();
    }
    
    /**
     * 新しいUUID IDを生成
     * 
     * @return UUID文字列
     */
    protected static String generateNewId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * ID値を取得
     * 
     * @return ID値
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        EntityId entityId = (EntityId) other;
        return Objects.equals(value, entityId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{value='" + value + "'}";
    }
}