package com.customermaster.domain.assign;

import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.shared.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 担当者変更申請
 * 
 * 法人の担当者変更申請に関する情報を管理する値オブジェクト
 * 担当者割り当てドメインの中核的な概念
 */
public class AssignmentChangeRequest extends ValueObject {
    
    private final UserId requesterId;
    private final AssignedSalesRepresentative currentAssignment;
    private final AssignedSalesRepresentative newAssignment;
    private final AssignmentChangeType changeType;
    private final String reason;
    private final LocalDateTime requestedAt;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private AssignmentChangeRequest() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param requesterId 申請者ID（必須）
     * @param currentAssignment 現在の担当者（必須）
     * @param newAssignment 新しい担当者（必須）
     * @param reason 変更理由（必須）
     * @param requestedAt 申請日時（必須）
     */
    private AssignmentChangeRequest(UserId requesterId, AssignedSalesRepresentative currentAssignment,
                                   AssignedSalesRepresentative newAssignment, String reason,
                                   LocalDateTime requestedAt) {
        this.requesterId = Objects.requireNonNull(requesterId, "申請者IDは必須です");
        this.currentAssignment = Objects.requireNonNull(currentAssignment, "現在の担当者は必須です");
        this.newAssignment = Objects.requireNonNull(newAssignment, "新しい担当者は必須です");
        this.reason = Objects.requireNonNull(reason, "変更理由は必須です");
        this.requestedAt = Objects.requireNonNull(requestedAt, "申請日時は必須です");
        
        // 変更種類を判定
        this.changeType = determineChangeType(currentAssignment, newAssignment);
        
        // ビジネスルール検証
        validateBusinessRules();
    }
    
    /**
     * 担当者変更申請を作成
     * 
     * @param requesterId 申請者ID
     * @param currentAssignment 現在の担当者
     * @param newSalesRepId 新しい営業担当者ID
     * @param newDepartmentId 新しい営業部ID
     * @param reason 変更理由
     * @return AssignmentChangeRequest
     */
    public static AssignmentChangeRequest create(UserId requesterId, 
                                               AssignedSalesRepresentative currentAssignment,
                                               UserId newSalesRepId, 
                                               SalesDepartmentId newDepartmentId,
                                               String reason) {
        AssignedSalesRepresentative newAssignment = AssignedSalesRepresentative.assignNow(
            newSalesRepId, newDepartmentId);
        
        return new AssignmentChangeRequest(requesterId, currentAssignment, newAssignment, 
                                         reason, LocalDateTime.now());
    }
    
    /**
     * 担当者変更申請を作成（申請日時指定）
     * 
     * @param requesterId 申請者ID
     * @param currentAssignment 現在の担当者
     * @param newAssignment 新しい担当者
     * @param reason 変更理由
     * @param requestedAt 申請日時
     * @return AssignmentChangeRequest
     */
    public static AssignmentChangeRequest of(UserId requesterId,
                                           AssignedSalesRepresentative currentAssignment,
                                           AssignedSalesRepresentative newAssignment,
                                           String reason,
                                           LocalDateTime requestedAt) {
        return new AssignmentChangeRequest(requesterId, currentAssignment, newAssignment, 
                                         reason, requestedAt);
    }
    
    /**
     * 変更種類を判定
     * 
     * @param current 現在の担当者
     * @param newAssignment 新しい担当者
     * @return 変更種類
     */
    private AssignmentChangeType determineChangeType(AssignedSalesRepresentative current,
                                                   AssignedSalesRepresentative newAssignment) {
        if (current.isCrossDepartmentChange(newAssignment.getDepartmentId())) {
            return AssignmentChangeType.CROSS_DEPARTMENT;
        } else {
            return AssignmentChangeType.WITHIN_DEPARTMENT;
        }
    }
    
    /**
     * ビジネスルールを検証
     */
    private void validateBusinessRules() {
        // 変更理由は空文字列や空白のみは不可
        if (reason.trim().isEmpty()) {
            throw new IllegalArgumentException("変更理由は必須です");
        }
        
        // 同じ担当者への変更は不可
        if (currentAssignment.getSalesRepresentativeId().equals(newAssignment.getSalesRepresentativeId())) {
            throw new IllegalArgumentException("同じ担当者への変更はできません");
        }
    }
    
    /**
     * 申請者が現在の担当者かどうかを判定
     * 
     * @return 申請者が現在の担当者の場合true
     */
    public boolean isRequestedByCurrentAssignee() {
        return currentAssignment.isAssignedTo(requesterId);
    }
    
    /**
     * 申請者が指定された営業部に所属しているかどうかを判定
     * 
     * @param departmentId 営業部ID
     * @return 申請者が指定された営業部に所属している場合true
     */
    public boolean isRequesterFromDepartment(SalesDepartmentId departmentId) {
        return currentAssignment.belongsToDepartment(departmentId);
    }
    
    /**
     * 承認が必要な営業部のリストを取得
     * 
     * @return 承認が必要な営業部IDの配列
     */
    public SalesDepartmentId[] getRequiredApprovalDepartments() {
        if (changeType.isCrossDepartment()) {
            // 営業部間変更の場合、両方の営業部の承認が必要
            return new SalesDepartmentId[] {
                currentAssignment.getDepartmentId(),
                newAssignment.getDepartmentId()
            };
        } else {
            // 営業部内変更の場合、現在の営業部の承認のみ必要
            return new SalesDepartmentId[] {
                currentAssignment.getDepartmentId()
            };
        }
    }
    
    // Getters
    public UserId getRequesterId() { return requesterId; }
    public AssignedSalesRepresentative getCurrentAssignment() { return currentAssignment; }
    public AssignedSalesRepresentative getNewAssignment() { return newAssignment; }
    public AssignmentChangeType getChangeType() { return changeType; }
    public String getReason() { return reason; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssignmentChangeRequest that = (AssignmentChangeRequest) other;
        return Objects.equals(requesterId, that.requesterId) &&
               Objects.equals(currentAssignment, that.currentAssignment) &&
               Objects.equals(newAssignment, that.newAssignment) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(requestedAt, that.requestedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(requesterId, currentAssignment, newAssignment, reason, requestedAt);
    }
    
    @Override
    public String toString() {
        return "AssignmentChangeRequest{" +
               "requesterId=" + requesterId +
               ", changeType=" + changeType +
               ", currentAssignment=" + currentAssignment.getSalesRepresentativeId() +
               ", newAssignment=" + newAssignment.getSalesRepresentativeId() +
               ", reason='" + reason + '\'' +
               ", requestedAt=" + requestedAt +
               '}';
    }
}