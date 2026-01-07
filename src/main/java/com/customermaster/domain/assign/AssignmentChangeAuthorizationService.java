package com.customermaster.domain.assign;

import com.customermaster.domain.customer.Customer;
import com.customermaster.domain.user.User;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;

import java.util.Objects;

/**
 * 担当者変更認可サービス
 * 
 * 担当者変更に関する権限チェックを行うドメインサービス
 * 担当者割り当てドメインの中核的なビジネスルールを管理
 */
public class AssignmentChangeAuthorizationService {
    
    /**
     * 担当者変更申請の権限を検証
     * 
     * @param requester 申請者
     * @param customer 対象法人
     * @throws IllegalArgumentException 権限がない場合
     */
    public void validateAssignmentChangeRequest(User requester, Customer customer) {
        Objects.requireNonNull(requester, "申請者は必須です");
        Objects.requireNonNull(customer, "対象法人は必須です");
        
        // システム管理者は常に申請可能
        if (requester.getRole().isSystemAdministrator()) {
            return; // 検証成功
        }
        
        // 営業担当者または部長のみ申請可能
        if (!requester.getRole().isSalesRepresentative() && !requester.getRole().isDepartmentManager()) {
            throw new IllegalArgumentException("営業担当者または部長のみが担当者変更を申請できます");
        }
        
        // 要件10.2: 営業担当者は自分が担当する法人の担当者変更を申請できる
        if (requester.getRole().isSalesRepresentative() && !requester.getRole().isDepartmentManager()) {
            if (!customer.isAssignedTo(requester.getId())) {
                throw new IllegalArgumentException("自分が担当する法人の担当者変更のみ申請できます");
            }
        }
        
        // 部長は同じ営業部の法人の担当者変更を申請できる
        if (requester.getRole().isDepartmentManager()) {
            if (requester.getDepartmentId() != null && !customer.belongsToDepartment(requester.getDepartmentId())) {
                throw new IllegalArgumentException("同じ営業部の法人の担当者変更のみ申請できます");
            }
        }
        
        // 承認待ち状態の法人は編集不可（要件3.16）
        if (customer.isPendingApproval()) {
            throw new IllegalArgumentException("承認待ちの法人は担当者変更できません");
        }
        
        // 検証成功（例外がスローされなければ成功）
    }
    
    /**
     * 担当者変更の直接実行権限を検証
     * 
     * @param executor 実行者
     * @param customer 対象法人
     * @param changeRequest 変更申請
     * @throws IllegalArgumentException 権限がない場合
     */
    public void validateDirectAssignmentChangeExecution(User executor, Customer customer,
                                                       AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(executor, "実行者は必須です");
        Objects.requireNonNull(customer, "対象法人は必須です");
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // 要件10.6: 情報システム部は営業部をまたいだ担当者変更を直接実行できる
        if (executor.getRole().isSystemAdministrator()) {
            return; // 検証成功
        }
        
        // 要件10.7: 部長は同じ営業部内の担当者変更を直接実行できる
        if (executor.getRole().isDepartmentManager() && changeRequest.getChangeType().isWithinDepartment()) {
            if (executor.getDepartmentId() != null && customer.belongsToDepartment(executor.getDepartmentId())) {
                return; // 検証成功
            }
        }
        
        throw new IllegalArgumentException("担当者変更を直接実行する権限がありません");
    }
    
    /**
     * 担当者変更申請の承認権限を検証
     * 
     * @param approver 承認者
     * @param changeRequest 変更申請
     * @throws IllegalArgumentException 権限がない場合
     */
    public void validateAssignmentChangeApproval(User approver, AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(approver, "承認者は必須です");
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // システム管理者は常に承認可能
        if (approver.getRole().isSystemAdministrator()) {
            return; // 検証成功
        }
        
        // 部長のみ承認可能
        if (!approver.getRole().isDepartmentManager()) {
            throw new IllegalArgumentException("部長のみが担当者変更申請を承認できます");
        }
        
        // 要件3.11: 申請者本人が部長の場合でも、自分では承認できず他の部長の承認が必要
        if (changeRequest.getRequesterId().equals(approver.getId())) {
            throw new IllegalArgumentException("申請者本人は承認できません");
        }
        
        // 承認が必要な営業部に所属している部長のみ承認可能
        SalesDepartmentId[] requiredDepartments = changeRequest.getRequiredApprovalDepartments();
        for (SalesDepartmentId departmentId : requiredDepartments) {
            if (departmentId.equals(approver.getDepartmentId())) {
                return; // 検証成功
            }
        }
        
        throw new IllegalArgumentException("この申請を承認する権限がありません");
    }
    
    /**
     * 担当者変更申請が有効かどうかをチェック
     * 
     * @param changeRequest 変更申請
     * @param customer 対象法人
     * @return 有効な場合true
     */
    public boolean isValidAssignmentChangeRequest(AssignmentChangeRequest changeRequest, Customer customer) {
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        Objects.requireNonNull(customer, "対象法人は必須です");
        
        // 現在の担当者が申請時の担当者と一致しているかチェック
        if (!customer.getAssignedSalesRep().equals(changeRequest.getCurrentAssignment())) {
            return false;
        }
        
        // 法人が編集可能な状態かチェック
        if (!customer.isEditable()) {
            return false;
        }
        
        return true;
    }
}