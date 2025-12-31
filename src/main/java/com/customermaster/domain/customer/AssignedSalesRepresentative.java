package com.customermaster.domain.customer;

import com.customermaster.domain.shared.ValueObject;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 担当営業者
 * 
 * 顧客に割り当てられた営業担当者の情報を管理する値オブジェクト
 */
public class AssignedSalesRepresentative extends ValueObject {
    
    private final UserId salesRepresentativeId;
    private final SalesDepartmentId departmentId;
    private final LocalDateTime assignedAt;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private AssignedSalesRepresentative() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param salesRepresentativeId 営業担当者ID（必須）
     * @param departmentId 営業部ID（必須）
     * @param assignedAt 割り当て日時（必須）
     */
    private AssignedSalesRepresentative(UserId salesRepresentativeId, SalesDepartmentId departmentId,
                                       LocalDateTime assignedAt) {
        this.salesRepresentativeId = Objects.requireNonNull(salesRepresentativeId, "営業担当者IDは必須です");
        this.departmentId = Objects.requireNonNull(departmentId, "営業部IDは必須です");
        this.assignedAt = Objects.requireNonNull(assignedAt, "割り当て日時は必須です");
    }
    
    /**
     * 担当営業者を作成
     * 
     * @param salesRepresentativeId 営業担当者ID
     * @param departmentId 営業部ID
     * @param assignedAt 割り当て日時
     * @return AssignedSalesRepresentative
     */
    public static AssignedSalesRepresentative of(UserId salesRepresentativeId, 
                                                SalesDepartmentId departmentId,
                                                LocalDateTime assignedAt) {
        return new AssignedSalesRepresentative(salesRepresentativeId, departmentId, assignedAt);
    }
    
    /**
     * 担当営業者を作成（現在時刻で割り当て）
     * 
     * @param salesRepresentativeId 営業担当者ID
     * @param departmentId 営業部ID
     * @return AssignedSalesRepresentative
     */
    public static AssignedSalesRepresentative assignNow(UserId salesRepresentativeId,
                                                       SalesDepartmentId departmentId) {
        return new AssignedSalesRepresentative(salesRepresentativeId, departmentId, LocalDateTime.now());
    }
    
    // Getters
    public UserId getSalesRepresentativeId() { return salesRepresentativeId; }
    public SalesDepartmentId getDepartmentId() { return departmentId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    
    /**
     * 指定された営業担当者かどうかを判定
     * 
     * @param userId ユーザーID
     * @return 指定された営業担当者の場合true
     */
    public boolean isAssignedTo(UserId userId) {
        return salesRepresentativeId.equals(userId);
    }
    
    /**
     * 指定された営業部に所属しているかどうかを判定
     * 
     * @param departmentId 営業部ID
     * @return 指定された営業部に所属している場合true
     */
    public boolean belongsToDepartment(SalesDepartmentId departmentId) {
        return this.departmentId.equals(departmentId);
    }
    
    /**
     * 担当者を変更
     * 
     * @param newSalesRepresentativeId 新しい営業担当者ID
     * @param newDepartmentId 新しい営業部ID
     * @return 新しいAssignedSalesRepresentative
     */
    public AssignedSalesRepresentative changeTo(UserId newSalesRepresentativeId,
                                               SalesDepartmentId newDepartmentId) {
        return new AssignedSalesRepresentative(newSalesRepresentativeId, newDepartmentId, LocalDateTime.now());
    }
    
    /**
     * 営業部間の担当者変更かどうかを判定
     * 
     * @param newDepartmentId 新しい営業部ID
     * @return 営業部間の変更の場合true
     */
    public boolean isCrossDepartmentChange(SalesDepartmentId newDepartmentId) {
        return !this.departmentId.equals(newDepartmentId);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssignedSalesRepresentative that = (AssignedSalesRepresentative) other;
        return Objects.equals(salesRepresentativeId, that.salesRepresentativeId) &&
               Objects.equals(departmentId, that.departmentId) &&
               Objects.equals(assignedAt, that.assignedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(salesRepresentativeId, departmentId, assignedAt);
    }
    
    @Override
    public String toString() {
        return "AssignedSalesRepresentative{" +
               "salesRepresentativeId=" + salesRepresentativeId +
               ", departmentId=" + departmentId +
               ", assignedAt=" + assignedAt +
               '}';
    }
}