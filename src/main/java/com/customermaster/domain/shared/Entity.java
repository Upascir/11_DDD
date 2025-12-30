package com.customermaster.domain.shared;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * エンティティの基底クラス
 * 
 * すべてのエンティティの共通実装を提供
 * 
 * @param <ID> エンティティIDの型
 */
public abstract class Entity<ID extends EntityId> {
    
    private final ID id;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * コンストラクタ（新規エンティティ用）
     * 
     * @param id エンティティID
     */
    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "IDは必須です");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * コンストラクタ（既存エンティティ復元用）
     * 
     * @param id エンティティID
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    protected Entity(ID id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "IDは必須です");
        this.createdAt = Objects.requireNonNull(createdAt, "作成日時は必須です");
        this.updatedAt = Objects.requireNonNull(updatedAt, "更新日時は必須です");
    }
    
    /**
     * エンティティIDを取得
     * 
     * @return エンティティID
     */
    public ID getId() {
        return id;
    }
    
    /**
     * 作成日時を取得
     * 
     * @return 作成日時
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * 更新日時を取得
     * 
     * @return 更新日時
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * 更新日時を現在時刻に設定
     */
    protected void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 同じエンティティかどうかを判定（IDベース）
     * 
     * @param other 比較対象
     * @return 同じエンティティの場合true
     */
    public boolean sameIdentityAs(Entity<ID> other) {
        return other != null && this.id.equals(other.id);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Entity<?> entity = (Entity<?>) other;
        return Objects.equals(id, entity.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "id=" + id +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}