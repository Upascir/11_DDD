package com.customermaster.domain.assign;

import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;

import java.util.List;
import java.util.Optional;

/**
 * 担当者変更申請リポジトリ
 * 
 * 担当者変更申請の永続化を担当するリポジトリインターフェース
 */
public interface AssignmentChangeRequestRepository {
    
    /**
     * 担当者変更申請を保存
     * 
     * @param request 担当者変更申請
     */
    void save(AssignmentChangeRequest request);
    
    /**
     * IDで担当者変更申請を検索
     * 
     * @param requestId 申請ID
     * @return 担当者変更申請（存在しない場合はEmpty）
     */
    Optional<AssignmentChangeRequest> findById(AssignmentChangeRequestId requestId);
    
    /**
     * 法人IDで担当者変更申請を検索
     * 
     * @param customerId 法人ID
     * @return 担当者変更申請のリスト
     */
    List<AssignmentChangeRequest> findByCustomerId(CustomerId customerId);
    
    /**
     * 申請者IDで担当者変更申請を検索
     * 
     * @param requesterId 申請者ID
     * @return 担当者変更申請のリスト
     */
    List<AssignmentChangeRequest> findByRequesterId(UserId requesterId);
    
    /**
     * 営業部IDで承認待ちの担当者変更申請を検索
     * 
     * @param departmentId 営業部ID
     * @return 承認待ちの担当者変更申請のリスト
     */
    List<AssignmentChangeRequest> findPendingByDepartmentId(SalesDepartmentId departmentId);
    
    /**
     * 承認待ちの担当者変更申請を全て検索
     * 
     * @return 承認待ちの担当者変更申請のリスト
     */
    List<AssignmentChangeRequest> findAllPending();
    
    /**
     * 期限切れの担当者変更申請を検索
     * 
     * @return 期限切れの担当者変更申請のリスト
     */
    List<AssignmentChangeRequest> findExpired();
    
    /**
     * 担当者変更申請を削除
     * 
     * @param requestId 申請ID
     */
    void deleteById(AssignmentChangeRequestId requestId);
    
    /**
     * 指定された法人に承認待ちの担当者変更申請が存在するかチェック
     * 
     * @param customerId 法人ID
     * @return 承認待ちの申請が存在する場合true
     */
    boolean existsPendingByCustomerId(CustomerId customerId);
}