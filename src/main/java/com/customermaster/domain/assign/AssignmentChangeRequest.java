package com.customermaster.domain.assign;

import com.customermaster.domain.user.UserId;
import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.shared.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 担当者変更申請集約
 * 
 * 法人の担当者変更申請に関する情報を管理する集約ルート
 * 担当者割り当てドメインの中核的な概念
 */
public class AssignmentChangeRequest extends Entity<AssignmentChangeRequestId> {
    
    private final CustomerId customerId;
    private final UserId requesterId;
    private final AssignedSalesRepresentative currentAssignment;
    private final AssignedSalesRepresentative newAssignment;
    private final AssignmentChangeType changeType;
    private final String reason;
    private final LocalDateTime requestedAt;
    private final LocalDateTime deadline;
    private AssignmentApprovalStatus status;
    private final List<AssignmentApproval> approvals;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private AssignmentChangeRequest() {
        super(null);
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param requestId 申請ID
     * @param customerId 法人ID
     * @param requesterId 申請者ID（必須）
     * @param currentAssignment 現在の担当者（必須）
     * @param newAssignment 新しい担当者（必須）
     * @param reason 変更理由（必須）
     * @param requestedAt 申請日時（必須）
     */
    private AssignmentChangeRequest(AssignmentChangeRequestId requestId, CustomerId customerId,
                                   UserId requesterId, AssignedSalesRepresentative currentAssignment,
                                   AssignedSalesRepresentative newAssignment, String reason,
                                   LocalDateTime requestedAt) {
        super(requestId);
        this.customerId = Objects.requireNonNull(customerId, "法人IDは必須です");
        this.requesterId = Objects.requireNonNull(requesterId, "申請者IDは必須です");
        this.currentAssignment = Objects.requireNonNull(currentAssignment, "現在の担当者は必須です");
        this.newAssignment = Objects.requireNonNull(newAssignment, "新しい担当者は必須です");
        this.reason = Objects.requireNonNull(reason, "変更理由は必須です");
        this.requestedAt = Objects.requireNonNull(requestedAt, "申請日時は必須です");
        
        // 変更種類を判定
        this.changeType = determineChangeType(currentAssignment, newAssignment);
        
        // 承認期限を設定（申請から1週間後）
        this.deadline = requestedAt.plusWeeks(1);
        
        // 初期状態は申請中
        this.status = AssignmentApprovalStatus.PENDING;
        this.approvals = new ArrayList<>();
        
        // ビジネスルール検証
        validateBusinessRules();
    }
    
    /**
     * 担当者変更申請を作成
     * 
     * @param customerId 法人ID
     * @param requesterId 申請者ID
     * @param currentAssignment 現在の担当者
     * @param newSalesRepId 新しい営業担当者ID
     * @param newDepartmentId 新しい営業部ID
     * @param reason 変更理由
     * @return AssignmentChangeRequest
     */
    public static AssignmentChangeRequest create(CustomerId customerId, UserId requesterId, 
                                               AssignedSalesRepresentative currentAssignment,
                                               UserId newSalesRepId, 
                                               SalesDepartmentId newDepartmentId,
                                               String reason) {
        AssignedSalesRepresentative newAssignment = AssignedSalesRepresentative.assignNow(
            newSalesRepId, newDepartmentId);
        
        return new AssignmentChangeRequest(AssignmentChangeRequestId.generate(), customerId,
                                         requesterId, currentAssignment, newAssignment, 
                                         reason, LocalDateTime.now());
    }
    
    /**
     * 担当者変更申請を復元（永続化からの復元用）
     * 
     * @param requestId 申請ID
     * @param customerId 法人ID
     * @param requesterId 申請者ID
     * @param currentAssignment 現在の担当者
     * @param newAssignment 新しい担当者
     * @param reason 変更理由
     * @param requestedAt 申請日時
     * @param status 承認ステータス
     * @param approvals 承認リスト
     * @return AssignmentChangeRequest
     */
    public static AssignmentChangeRequest restore(AssignmentChangeRequestId requestId,
                                                CustomerId customerId,
                                                UserId requesterId,
                                                AssignedSalesRepresentative currentAssignment,
                                                AssignedSalesRepresentative newAssignment,
                                                String reason,
                                                LocalDateTime requestedAt,
                                                AssignmentApprovalStatus status,
                                                List<AssignmentApproval> approvals) {
        AssignmentChangeRequest request = new AssignmentChangeRequest(requestId, customerId,
                                                                    requesterId, currentAssignment, 
                                                                    newAssignment, reason, requestedAt);
        request.status = status;
        request.approvals.addAll(approvals);
        return request;
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
     * 担当者変更申請を承認
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param comment 承認コメント
     * @throws IllegalStateException 承認できない状態の場合
     */
    public void approve(UserId approverId, SalesDepartmentId approverDepartmentId, String comment) {
        // 承認可能な状態かチェック
        if (!status.isPending()) {
            throw new IllegalStateException("承認待ち状態でない申請は承認できません");
        }
        
        // 承認期限チェック
        if (isExpired()) {
            this.status = AssignmentApprovalStatus.EXPIRED;
            throw new IllegalStateException("承認期限を過ぎた申請は承認できません");
        }
        
        // 承認権限チェック（承認が必要な営業部に所属しているかチェック）
        SalesDepartmentId[] requiredDepartments = getRequiredApprovalDepartments();
        boolean hasAuthority = false;
        for (SalesDepartmentId departmentId : requiredDepartments) {
            if (departmentId.equals(approverDepartmentId)) {
                hasAuthority = true;
                break;
            }
        }
        if (!hasAuthority) {
            throw new IllegalArgumentException("この申請を承認する権限がありません");
        }
        
        // 既に同じ営業部から承認されていないかチェック
        for (AssignmentApproval approval : approvals) {
            if (approval.isApprovedByDepartment(approverDepartmentId)) {
                throw new IllegalStateException("既に同じ営業部から承認されています");
            }
        }
        
        // 承認を追加
        AssignmentApproval approval = AssignmentApproval.approve(approverId, approverDepartmentId, comment);
        approvals.add(approval);
        
        // ステータスを更新
        updateStatusAfterApproval();
    }
    
    /**
     * 担当者変更申請を却下
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param reason 却下理由
     * @throws IllegalStateException 却下できない状態の場合
     */
    public void reject(UserId approverId, SalesDepartmentId approverDepartmentId, String reason) {
        // 却下可能な状態かチェック
        if (!status.isPending()) {
            throw new IllegalStateException("承認待ち状態でない申請は却下できません");
        }
        
        // 承認権限チェック
        SalesDepartmentId[] requiredDepartments = getRequiredApprovalDepartments();
        boolean hasAuthority = false;
        for (SalesDepartmentId departmentId : requiredDepartments) {
            if (departmentId.equals(approverDepartmentId)) {
                hasAuthority = true;
                break;
            }
        }
        if (!hasAuthority) {
            throw new IllegalArgumentException("この申請を却下する権限がありません");
        }
        
        // 却下を追加
        AssignmentApproval rejection = AssignmentApproval.reject(approverId, approverDepartmentId, reason);
        approvals.add(rejection);
        
        // ステータスを却下に変更
        this.status = AssignmentApprovalStatus.REJECTED;
    }
    
    /**
     * 申請内容を更新（再申請）
     * 
     * @param newAssignment 新しい担当者
     * @param newReason 新しい変更理由
     * @throws IllegalStateException 更新できない状態の場合
     */
    public void updateRequest(AssignedSalesRepresentative newAssignment, String newReason) {
        // 編集可能な状態かチェック
        if (!status.isEditable()) {
            throw new IllegalStateException("編集できない状態です");
        }
        
        // 新しい内容で更新（不変オブジェクトなので新しいインスタンスを作成する必要があるが、
        // ここでは簡略化のため例外をスローする）
        throw new UnsupportedOperationException("申請内容の更新は新しい申請として作成してください");
    }
    
    /**
     * 承認期限が過ぎているかどうかを判定
     * 
     * @return 承認期限が過ぎている場合true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }
    
    /**
     * 承認後のステータス更新
     */
    private void updateStatusAfterApproval() {
        SalesDepartmentId[] requiredDepartments = getRequiredApprovalDepartments();
        
        // 必要な全ての営業部から承認を得られているかチェック
        int approvedDepartmentCount = 0;
        for (SalesDepartmentId departmentId : requiredDepartments) {
            for (AssignmentApproval approval : approvals) {
                if (approval.isApprovedByDepartment(departmentId)) {
                    approvedDepartmentCount++;
                    break;
                }
            }
        }
        
        if (approvedDepartmentCount == requiredDepartments.length) {
            // 全ての必要な承認を得られた場合
            this.status = AssignmentApprovalStatus.APPROVED;
        } else if (changeType.isCrossDepartment() && approvedDepartmentCount > 0) {
            // 営業部間変更で一部の承認を得られた場合
            this.status = AssignmentApprovalStatus.PARTIALLY_APPROVED;
        }
        // それ以外は PENDING のまま
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
    public CustomerId getCustomerId() { return customerId; }
    public UserId getRequesterId() { return requesterId; }
    public AssignedSalesRepresentative getCurrentAssignment() { return currentAssignment; }
    public AssignedSalesRepresentative getNewAssignment() { return newAssignment; }
    public AssignmentChangeType getChangeType() { return changeType; }
    public String getReason() { return reason; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public AssignmentApprovalStatus getStatus() { return status; }
    
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
     * 完全に承認されているかどうかを判定
     * 
     * @return 完全に承認されている場合true
     */
    public boolean isFullyApproved() {
        return status.isApproved();
    }
    
    /**
     * 承認待ち状態かどうかを判定
     * 
     * @return 承認待ち状態の場合true
     */
    public boolean isPending() {
        return status.isPending();
    }
    
    /**
     * 却下されているかどうかを判定
     * 
     * @return 却下されている場合true
     */
    public boolean isRejected() {
        return status.isRejected();
    }
    
    /**
     * 編集可能かどうかを判定
     * 
     * @return 編集可能な場合true
     */
    public boolean isEditable() {
        return status.isEditable();
    }
    
    /**
     * 指定された営業部から承認済みかどうかを判定
     * 
     * @param departmentId 営業部ID
     * @return 指定された営業部から承認済みの場合true
     */
    public boolean isApprovedByDepartment(SalesDepartmentId departmentId) {
        for (AssignmentApproval approval : approvals) {
            if (approval.isApprovedByDepartment(departmentId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 承認履歴を取得
     * 
     * @return 承認履歴のコピー
     */
    public List<AssignmentApproval> getApprovals() {
        return new ArrayList<>(approvals);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssignmentChangeRequest that = (AssignmentChangeRequest) other;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    @Override
    public String toString() {
        return "AssignmentChangeRequest{" +
               "id=" + getId() +
               ", customerId=" + customerId +
               ", requesterId=" + requesterId +
               ", changeType=" + changeType +
               ", status=" + status +
               ", currentAssignment=" + currentAssignment.getSalesRepresentativeId() +
               ", newAssignment=" + newAssignment.getSalesRepresentativeId() +
               ", reason='" + reason + '\'' +
               ", requestedAt=" + requestedAt +
               ", deadline=" + deadline +
               '}';
    }
}