package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.shared.Entity;
import com.customermaster.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 営業部
 * 
 * 営業担当者と部長を管理する組織単位を表現するエンティティ（集約ルート）
 */
public class SalesDepartment extends Entity<SalesDepartmentId> {
    
    private DepartmentName departmentName;
    private Set<UserId> salesRepresentatives;
    private Set<UserId> departmentManagers;
    private DepartmentStatus status;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private SalesDepartment() {
        super(null);
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ（新規営業部用）
     * 
     * @param departmentId 営業部ID
     * @param departmentName 部署名
     * @param initialManager 初期部長のユーザーID
     */
    private SalesDepartment(SalesDepartmentId departmentId, DepartmentName departmentName, UserId initialManager) {
        super(departmentId);
        this.departmentName = Objects.requireNonNull(departmentName, "部署名は必須です");
        this.salesRepresentatives = new HashSet<>();
        this.departmentManagers = new HashSet<>();
        this.status = DepartmentStatus.ACTIVE;
        
        // 初期部長を設定（部長は営業担当者でもある）
        Objects.requireNonNull(initialManager, "初期部長は必須です");
        this.salesRepresentatives.add(initialManager);
        this.departmentManagers.add(initialManager);
    }
    
    /**
     * コンストラクタ（既存営業部復元用）
     * 
     * @param departmentId 営業部ID
     * @param departmentName 部署名
     * @param salesRepresentatives 営業担当者リスト
     * @param departmentManagers 部長リスト
     * @param status 部署状態
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    private SalesDepartment(SalesDepartmentId departmentId, DepartmentName departmentName,
                           Set<UserId> salesRepresentatives, Set<UserId> departmentManagers,
                           DepartmentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(departmentId, createdAt, updatedAt);
        this.departmentName = Objects.requireNonNull(departmentName, "部署名は必須です");
        this.salesRepresentatives = new HashSet<>(Objects.requireNonNull(salesRepresentatives, "営業担当者リストは必須です"));
        this.departmentManagers = new HashSet<>(Objects.requireNonNull(departmentManagers, "部長リストは必須です"));
        this.status = Objects.requireNonNull(status, "部署状態は必須です");
        
        validateInvariants();
    }
    
    /**
     * 新規営業部を作成
     * 
     * @param departmentId 営業部ID
     * @param departmentName 部署名
     * @param initialManager 初期部長のユーザーID
     * @return SalesDepartment
     */
    public static SalesDepartment create(SalesDepartmentId departmentId, DepartmentName departmentName, UserId initialManager) {
        return new SalesDepartment(departmentId, departmentName, initialManager);
    }
    
    /**
     * 既存営業部を復元
     * 
     * @param departmentId 営業部ID
     * @param departmentName 部署名
     * @param salesRepresentatives 営業担当者リスト
     * @param departmentManagers 部長リスト
     * @param status 部署状態
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return SalesDepartment
     */
    public static SalesDepartment restore(SalesDepartmentId departmentId, DepartmentName departmentName,
                                         Set<UserId> salesRepresentatives, Set<UserId> departmentManagers,
                                         DepartmentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new SalesDepartment(departmentId, departmentName, salesRepresentatives, 
                                  departmentManagers, status, createdAt, updatedAt);
    }
    
    // ビジネスメソッド
    
    /**
     * 営業担当者を追加
     * 
     * @param userId 追加する営業担当者のユーザーID
     */
    public void addSalesRepresentative(UserId userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        if (status != DepartmentStatus.ACTIVE) {
            throw new IllegalStateException("非アクティブな営業部には営業担当者を追加できません");
        }
        
        this.salesRepresentatives.add(userId);
        markAsUpdated();
    }
    
    /**
     * 営業担当者を削除
     * 
     * @param userId 削除する営業担当者のユーザーID
     */
    public void removeSalesRepresentative(UserId userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        // 部長の場合は先に部長から外す必要がある
        if (departmentManagers.contains(userId)) {
            throw new IllegalStateException("部長は営業担当者から削除する前に部長の役割を解除してください");
        }
        
        this.salesRepresentatives.remove(userId);
        markAsUpdated();
    }
    
    /**
     * 部長を追加
     * 
     * @param userId 部長に任命する営業担当者のユーザーID
     */
    public void addDepartmentManager(UserId userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        if (status != DepartmentStatus.ACTIVE) {
            throw new IllegalStateException("非アクティブな営業部には部長を追加できません");
        }
        
        // 部長は営業担当者である必要がある
        if (!salesRepresentatives.contains(userId)) {
            throw new IllegalArgumentException("部長は営業担当者の中から選出される必要があります");
        }
        
        this.departmentManagers.add(userId);
        markAsUpdated();
    }
    
    /**
     * 部長を削除
     * 
     * @param userId 部長から外す営業担当者のユーザーID
     */
    public void removeDepartmentManager(UserId userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        // 最後の部長は削除できない
        if (departmentManagers.size() <= 1) {
            throw new IllegalStateException("営業部には最低1人の部長が必要です");
        }
        
        this.departmentManagers.remove(userId);
        markAsUpdated();
    }
    
    /**
     * 指定されたユーザーが営業担当者かどうかを判定
     * 
     * @param userId ユーザーID
     * @return 営業担当者の場合true
     */
    public boolean isSalesRepresentative(UserId userId) {
        return salesRepresentatives.contains(userId);
    }
    
    /**
     * 指定されたユーザーが部長かどうかを判定
     * 
     * @param userId ユーザーID
     * @return 部長の場合true
     */
    public boolean isDepartmentManager(UserId userId) {
        return departmentManagers.contains(userId);
    }
    
    /**
     * 営業部をアクティブ化
     */
    public void activate() {
        this.status = DepartmentStatus.ACTIVE;
        markAsUpdated();
    }
    
    /**
     * 営業部を非アクティブ化
     */
    public void deactivate() {
        this.status = DepartmentStatus.INACTIVE;
        markAsUpdated();
    }
    
    /**
     * 営業部を削除（論理削除）
     */
    public void delete() {
        this.status = DepartmentStatus.DELETED;
        markAsUpdated();
    }
    
    /**
     * 部署名を変更
     * 
     * @param newDepartmentName 新しい部署名
     */
    public void changeDepartmentName(DepartmentName newDepartmentName) {
        this.departmentName = Objects.requireNonNull(newDepartmentName, "部署名は必須です");
        markAsUpdated();
    }
    
    /**
     * 営業部がアクティブかどうかを判定
     * 
     * @return アクティブな場合true
     */
    public boolean isActive() {
        return status == DepartmentStatus.ACTIVE;
    }
    
    /**
     * 営業担当者数を取得
     * 
     * @return 営業担当者数
     */
    public int getSalesRepresentativeCount() {
        return salesRepresentatives.size();
    }
    
    /**
     * 部長数を取得
     * 
     * @return 部長数
     */
    public int getDepartmentManagerCount() {
        return departmentManagers.size();
    }
    
    /**
     * 不変条件を検証
     */
    private void validateInvariants() {
        // 営業部には最低1人の部長が設定されている
        if (departmentManagers.isEmpty()) {
            throw new IllegalStateException("営業部には最低1人の部長が必要です");
        }
        
        // 部長は営業担当者の中から選出される
        for (UserId managerId : departmentManagers) {
            if (!salesRepresentatives.contains(managerId)) {
                throw new IllegalStateException("部長は営業担当者の中から選出される必要があります");
            }
        }
    }
    
    // Getters
    
    public DepartmentName getDepartmentName() { return departmentName; }
    public Set<UserId> getSalesRepresentatives() { return Collections.unmodifiableSet(salesRepresentatives); }
    public Set<UserId> getDepartmentManagers() { return Collections.unmodifiableSet(departmentManagers); }
    public DepartmentStatus getStatus() { return status; }
    
    @Override
    public String toString() {
        return "SalesDepartment{" +
               "id=" + getId() +
               ", departmentName=" + departmentName +
               ", salesRepresentatives=" + salesRepresentatives.size() + " members" +
               ", departmentManagers=" + departmentManagers.size() + " managers" +
               ", status=" + status +
               '}';
    }
}