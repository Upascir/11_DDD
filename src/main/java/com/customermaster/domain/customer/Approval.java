package com.customermaster.domain.customer;

import com.customermaster.domain.shared.ValueObject;
import com.customermaster.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 承認情報
 * 
 * 変更申請に対する承認または却下の情報を管理する値オブジェクト
 */
public class Approval extends ValueObject {
    
    private final UserId approverId;
    private final ApprovalStatus status;
    private final String comment;
    private final LocalDateTime processedAt;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private Approval() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param approverId 承認者ID（PENDING状態ではnull可）
     * @param status 承認状態
     * @param comment コメント（承認時は承認コメント、却下時は却下理由、PENDING時はnull可）
     * @param processedAt 処理日時（PENDING状態ではnull可）
     */
    private Approval(UserId approverId, ApprovalStatus status, String comment, LocalDateTime processedAt) {
        // PENDING状態以外では承認者IDと処理日時は必須
        if (status != ApprovalStatus.PENDING) {
            this.approverId = Objects.requireNonNull(approverId, "承認者IDは必須です");
            this.processedAt = Objects.requireNonNull(processedAt, "処理日時は必須です");
        } else {
            this.approverId = approverId;  // PENDING時はnull可
            this.processedAt = processedAt;  // PENDING時はnull可
        }
        this.status = Objects.requireNonNull(status, "承認状態は必須です");
        this.comment = validateComment(comment, status);
    }
    
    /**
     * 承認情報を作成
     * 
     * @param approverId 承認者ID
     * @param comment 承認コメント
     * @return Approval
     */
    public static Approval approve(UserId approverId, String comment) {
        return new Approval(approverId, ApprovalStatus.APPROVED, comment, LocalDateTime.now());
    }
    
    /**
     * 却下情報を作成
     * 
     * @param approverId 承認者ID
     * @param reason 却下理由
     * @return Approval
     */
    public static Approval reject(UserId approverId, String reason) {
        return new Approval(approverId, ApprovalStatus.REJECTED, reason, LocalDateTime.now());
    }
    
    /**
     * 期限切れ情報を作成
     * 
     * @return Approval
     */
    public static Approval expire() {
        return new Approval(
            UserId.of("SYSTEM"), 
            ApprovalStatus.EXPIRED, 
            "承認期限を過ぎたため自動的に却下されました", 
            LocalDateTime.now()
        );
    }
    
    /**
     * 承認待ち情報を作成
     * 
     * @return Approval
     */
    public static Approval createPending() {
        return new Approval(
            null,  // 承認待ち状態では承認者はまだ決まっていない
            ApprovalStatus.PENDING, 
            null,  // 承認待ち状態ではコメントはない
            null   // 承認待ち状態では処理日時はない
        );
    }
    
    /**
     * コメントの妥当性を検証
     * 
     * @param comment コメント
     * @param status 承認状態
     * @return 検証済みコメント
     */
    private String validateComment(String comment, ApprovalStatus status) {
        if (status == ApprovalStatus.APPROVED || status == ApprovalStatus.REJECTED) {
            if (comment == null || comment.trim().isEmpty()) {
                String action = status == ApprovalStatus.APPROVED ? "承認" : "却下";
                throw new IllegalArgumentException(action + "時はコメントが必須です");
            }
            if (comment.length() > 500) {
                throw new IllegalArgumentException("コメントは500文字以内で入力してください");
            }
        }
        // PENDING状態とEXPIRED状態ではコメントは任意
        return comment != null ? comment.trim() : null;
    }
    
    // Getters
    public UserId getApproverId() { return approverId; }
    public ApprovalStatus getStatus() { return status; }
    public String getComment() { return comment; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    
    /**
     * 承認かどうかを判定
     * 
     * @return 承認の場合true
     */
    public boolean isApproved() {
        return status.isApproved();
    }
    
    /**
     * 却下かどうかを判定
     * 
     * @return 却下の場合true
     */
    public boolean isRejected() {
        return status.isRejected();
    }
    
    /**
     * 期限切れかどうかを判定
     * 
     * @return 期限切れの場合true
     */
    public boolean isExpired() {
        return status.isExpired();
    }
    
    /**
     * 承認待ちかどうかを判定
     * 
     * @return 承認待ちの場合true
     */
    public boolean isPending() {
        return status.isPending();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Approval approval = (Approval) other;
        return Objects.equals(approverId, approval.approverId) &&
               status == approval.status &&
               Objects.equals(comment, approval.comment) &&
               Objects.equals(processedAt, approval.processedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(approverId, status, comment, processedAt);
    }
    
    @Override
    public String toString() {
        return "Approval{" +
               "approverId=" + approverId +
               ", status=" + status +
               ", comment='" + comment + '\'' +
               ", processedAt=" + processedAt +
               '}';
    }
}