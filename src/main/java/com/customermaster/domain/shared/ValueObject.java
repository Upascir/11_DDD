package com.customermaster.domain.shared;

/**
 * 値オブジェクトの基底クラス
 * 
 * 値オブジェクトの基本的な性質を定義：
 * - 不変性（Immutable）
 * - 等価性（Equality）
 * - 副作用なし（Side-effect free）
 */
public abstract class ValueObject {
    
    /**
     * 値オブジェクトの等価性を判定
     * 
     * @param other 比較対象
     * @return 等価の場合true
     */
    @Override
    public abstract boolean equals(Object other);
    
    /**
     * ハッシュコードを取得
     * 
     * @return ハッシュコード
     */
    @Override
    public abstract int hashCode();
    
    /**
     * 文字列表現を取得
     * 
     * @return 文字列表現
     */
    @Override
    public abstract String toString();
}