package com.customermaster.domain.assign;

import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.shared.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 担当者変更申請の承認情報
 * 
 * 承認者、承認日時、承認コメントなどの承認に関する情報を管理する値オブジェクト
 */
public class AssignmentApproval extends ValueObject {
    
    private final UserId approverId;
    private final SalesDepartmentId approverDepartmentId;
    private final String comment;
    private final LocalDateTime approvedAt;
    private final boolean isApproval; // true: 承認, false: 却下
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private AssignmentApproval() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param comment 承認コメント
     * @param approvedAt 承認日時
     * @param isApproval 承認フラグ
     */
    private AssignmentApproval(UserId approverId, SalesDepartmentId approverDepartmentId,
                              String comment, LocalDateTime approvedAt, boolean isApproval) {
        this.approverId = Objects.requireNonNull(approverId, "承認者IDは必須です");
        this.approverDepartmentId = Objects.requireNonNull(approverDepartmentId, "承認者の営業部IDは必須です");
        this.comment = Objects.requireNonNull(comment, "コメントは必須です");
        this.approvedAt = Objects.requireNonNull(approvedAt, "承認日時は必須です");
        this.isApproval = isApproval;
        
        // ビジネスルール検証
        validateBusinessRules();
    }
    
    /**
     * 承認情報を作成
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param comment 承認コメント
     * @return AssignmentApproval
     */
    public static AssignmentApproval approve(UserId approverId, SalesDepartmentId approverDepartmentId,
                                           String comment) {
        return new AssignmentApproval(approverId, approverDepartmentId, comment, 
                                    LocalDateTime.now(), true);
    }
    
    /**
     * 却下情報を作成
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param reason 却下理由
     * @return AssignmentApproval
     */
    public static AssignmentApproval reject(UserId approverId, SalesDepartmentId approverDepartmentId,
                                          String reason) {
        return new AssignmentApproval(approverId, approverDepartmentId, reason, 
                                    LocalDateTime.now(), false);
    }
    
    /**
     * 承認情報を作成（日時指定）
     * 
     * @param approverId 承認者ID
     * @param approverDepartmentId 承認者の営業部ID
     * @param comment 承認コメント
     * @param approvedAt 承認日時
     * @param isApproval 承認フラグ
     * @return AssignmentApproval
     */
    public static AssignmentApproval of(UserId approverId, SalesDepartmentId approverDepartmentId,
                                      String comment, LocalDateTime approvedAt, boolean isApproval) {
        return new AssignmentApproval(approverId, approverDepartmentId, comment, approvedAt, isApproval);
    }
    
    /**
     * ビジネスルールを検証
     */
    private void validateBusinessRules() {
        // コメントは空文字列や空白のみは不可
        if (comment.trim().isEmpty()) {
            String action = isApproval ? "承認" : "却下";
            throw new IllegalArgumentException(action + "コメントは必須です");
        }
    }
    
    /**
     * 承認かどうかを判定
     * 
     * @return 承認の場合true
     */
    public boolean isApproval() {
        return isApproval;
    }
    
    /**
     * 却下かどうかを判定
     * 
     * @return 却下の場合true
     */
    public boolean isRejection() {
        return !isApproval;
    }
    
    /**
     * 指定された営業部による承認かどうかを判定
     * 
     * @param departmentId 営業部ID
     * @return 指定された営業部による承認の場合true
     */
    public boolean isApprovedByDepartment(SalesDepartmentId departmentId) {
        return isApproval && approverDepartmentId.equals(departmentId);
    }
    
    // Getters
    public UserId getApproverId() { return approverId; }
    public SalesDepartmentId getApproverDepartmentId() { return approverDepartmentId; }
    public String getComment() { return comment; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssignmentApproval that = (AssignmentApproval) other;
        return isApproval == that.isApproval &&
               Objects.equals(approverId, that.approverId) &&
               Objects.equals(approverDepartmentId, that.approverDepartmentId) &&
               Objects.equals(comment, that.comment) &&
               Objects.equals(approvedAt, that.approvedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(approverId, approverDepartmentId, comment, approvedAt, isApproval);
    }
    
    @Override
    public String toString() {
        String action = isApproval ? "承認" : "却下";
        return "AssignmentApproval{" +
               "action=" + action +
               ", approverId=" + approverId +
               ", approverDepartmentId=" + approverDepartmentId +
               ", comment='" + comment + '\'' +
               ", approvedAt=" + approvedAt +
               '}';
    }
}