package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Entity;
import com.customermaster.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 顧客変更申請
 * 
 * 顧客情報の変更申請を管理する集約ルート
 * 承認ワークフローの中核となるエンティティ
 */
public class CustomerChangeRequest extends Entity<CustomerChangeRequestId> {
    
    private final CustomerId customerId;
    private final CustomerSnapshot originalData;
    private CustomerSnapshot proposedData;
    private final UserId requesterId;
    private Approval approval;  // 承認情報（常に非null、初期状態はPENDING）
    private final LocalDateTime requestedAt;
    private final LocalDateTime deadline;
    private String requestReason;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private CustomerChangeRequest() {
        super(null);
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param requestId 申請ID
     * @param customerId 顧客ID
     * @param originalData 変更前データ
     * @param proposedData 変更後データ
     * @param requesterId 申請者ID
     * @param requestReason 申請理由
     */
    private CustomerChangeRequest(CustomerChangeRequestId requestId, CustomerId customerId,
                                CustomerSnapshot originalData, CustomerSnapshot proposedData,
                                UserId requesterId, String requestReason) {
        super(requestId);
        this.customerId = Objects.requireNonNull(customerId, "顧客IDは必須です");
        this.originalData = Objects.requireNonNull(originalData, "変更前データは必須です");
        this.proposedData = Objects.requireNonNull(proposedData, "変更後データは必須です");
        this.requesterId = Objects.requireNonNull(requesterId, "申請者IDは必須です");
        this.requestReason = validateRequestReason(requestReason);
        this.approval = Approval.createPending();  // PENDING状態のApprovalを作成
        this.requestedAt = LocalDateTime.now();
        this.deadline = calculateDeadline(this.requestedAt);
        
        validateProposedData();
    }
    
    /**
     * 顧客変更申請を作成
     * 
     * @param customerId 顧客ID
     * @param originalData 変更前データ
     * @param proposedData 変更後データ
     * @param requesterId 申請者ID
     * @param requestReason 申請理由
     * @return CustomerChangeRequest
     */
    public static CustomerChangeRequest create(CustomerId customerId, CustomerSnapshot originalData,
                                             CustomerSnapshot proposedData, UserId requesterId,
                                             String requestReason) {
        return new CustomerChangeRequest(
            CustomerChangeRequestId.generate(),
            customerId,
            originalData,
            proposedData,
            requesterId,
            requestReason
        );
    }
    
    /**
     * 申請を承認
     * 
     * @param approverId 承認者ID
     * @param comment 承認コメント
     * @throws IllegalStateException 承認できない状態の場合
     */
    public void approve(UserId approverId, String comment) {
        validateCanProcess();
        validateNotSelfApproval(approverId);
        
        this.approval = Approval.approve(approverId, comment);
    }
    
    /**
     * 申請を却下
     * 
     * @param approverId 承認者ID
     * @param reason 却下理由
     * @throws IllegalStateException 却下できない状態の場合
     */
    public void reject(UserId approverId, String reason) {
        validateCanProcess();
        validateNotSelfApproval(approverId);
        
        this.approval = Approval.reject(approverId, reason);
    }
    
    /**
     * 申請内容を更新（承認待ち状態でのみ可能）
     * 
     * @param newProposedData 新しい変更後データ
     * @param newRequestReason 新しい申請理由
     * @throws IllegalStateException 更新できない状態の場合
     */
    public void updateProposedData(CustomerSnapshot newProposedData, String newRequestReason) {
        if (!isPending()) {
            throw new IllegalStateException("承認待ち状態でのみ申請内容を更新できます");
        }
        
        this.proposedData = Objects.requireNonNull(newProposedData, "変更後データは必須です");
        this.requestReason = validateRequestReason(newRequestReason);
        validateProposedData();
    }
    
    /**
     * 期限切れによる自動却下
     * 
     * @throws IllegalStateException 期限切れ処理できない状態の場合
     */
    public void expireByDeadline() {
        if (!isPending()) {
            throw new IllegalStateException("承認待ち状態でのみ期限切れ処理できます");
        }
        
        if (!isExpired()) {
            throw new IllegalStateException("まだ期限切れではありません");
        }
        
        this.approval = Approval.expire();
    }
    
    /**
     * 期限切れかどうかを判定
     * 
     * @return 期限切れの場合true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }
    
    /**
     * 承認待ち状態かどうかを判定
     * 
     * @return 承認待ちの場合true
     */
    public boolean isPending() {
        return approval.isPending();
    }
    
    /**
     * 承認済み状態かどうかを判定
     * 
     * @return 承認済みの場合true
     */
    public boolean isApproved() {
        return approval.isApproved();
    }
    
    /**
     * 却下状態かどうかを判定
     * 
     * @return 却下の場合true
     */
    public boolean isRejected() {
        return approval.isRejected();
    }
    
    /**
     * 期限切れ状態かどうかを判定
     * 
     * @return 期限切れの場合true
     */
    public boolean isExpiredStatus() {
        return approval.isExpired();
    }
    
    /**
     * 申請理由の妥当性を検証
     * 
     * @param reason 申請理由
     * @return 検証済み申請理由
     */
    private String validateRequestReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("申請理由は必須です");
        }
        if (reason.length() > 500) {
            throw new IllegalArgumentException("申請理由は500文字以内で入力してください");
        }
        return reason.trim();
    }
    
    /**
     * 変更後データの妥当性を検証
     */
    private void validateProposedData() {
        if (!originalData.getCustomerId().equals(proposedData.getCustomerId())) {
            throw new IllegalArgumentException("顧客IDは変更できません");
        }
        
        if (originalData.equals(proposedData)) {
            throw new IllegalArgumentException("変更前後のデータが同じです");
        }
    }
    
    /**
     * 処理可能状態かどうかを検証
     */
    private void validateCanProcess() {
        if (!isPending()) {
            throw new IllegalStateException("承認待ち状態でのみ処理できます");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("期限切れのため処理できません");
        }
    }
    
    /**
     * 自己承認でないことを検証
     * 
     * @param approverId 承認者ID
     */
    private void validateNotSelfApproval(UserId approverId) {
        if (requesterId.equals(approverId)) {
            throw new IllegalArgumentException("申請者本人は承認・却下できません");
        }
    }
    
    /**
     * 承認期限を計算（申請から1週間後）
     * 
     * @param requestedAt 申請日時
     * @return 承認期限
     */
    private LocalDateTime calculateDeadline(LocalDateTime requestedAt) {
        return requestedAt.plusWeeks(1);
    }
    
    // Getters
    public CustomerId getCustomerId() { return customerId; }
    public CustomerSnapshot getOriginalData() { return originalData; }
    public CustomerSnapshot getProposedData() { return proposedData; }
    public UserId getRequesterId() { return requesterId; }
    
    /**
     * 承認状態を取得
     * 
     * @return 承認状態
     */
    public ApprovalStatus getStatus() { 
        return approval.getStatus(); 
    }
    
    public Approval getApproval() { return approval; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getRequestReason() { return requestReason; }
    
    @Override
    public String toString() {
        return "CustomerChangeRequest{" +
               "id=" + getId() +
               ", customerId=" + customerId +
               ", requesterId=" + requesterId +
               ", status=" + getStatus() +
               ", requestedAt=" + requestedAt +
               ", deadline=" + deadline +
               '}';
    }
}