package com.customermaster.domain.shared;

import java.util.Objects;

/**
 * 名前の基底クラス
 * 
 * 名前に関する共通的なバリデーションと操作を提供
 */
public abstract class Name extends ValueObject {
    
    private final String value;
    
    /**
     * コンストラクタ
     * 
     * @param value 名前
     * @throws IllegalArgumentException 名前がnull、空、または不正な場合
     */
    protected Name(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("名前は必須です");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() > getMaxLength()) {
            throw new IllegalArgumentException(
                String.format("名前は%d文字以内で入力してください", getMaxLength())
            );
        }
        
        this.value = trimmedValue;
    }
    
    /**
     * 最大文字数を取得（サブクラスでオーバーライド）
     * 
     * @return 最大文字数
     */
    protected abstract int getMaxLength();
    
    /**
     * 名前の値を取得
     * 
     * @return 名前
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Name name = (Name) other;
        return Objects.equals(value, name.value);
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