package com.customermaster.domain.assign;

import com.customermaster.domain.customer.Customer;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.Role;
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
     * 担当者変更申請の権限をチェック
     * 
     * @param requesterId 申請者ID
     * @param requesterRole 申請者の役割
     * @param requesterDepartmentId 申請者の営業部ID
     * @param customer 対象顧客
     * @param changeRequest 変更申請
     * @return 申請可能な場合true
     * @throws IllegalArgumentException 権限がない場合
     */
    public boolean canRequestAssignmentChange(UserId requesterId, Role requesterRole,
                                            SalesDepartmentId requesterDepartmentId,
                                            Customer customer,
                                            AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(requesterId, "申請者IDは必須です");
        Objects.requireNonNull(requesterRole, "申請者の役割は必須です");
        Objects.requireNonNull(customer, "対象顧客は必須です");
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // システム管理者は常に申請可能
        if (requesterRole.isSystemAdministrator()) {
            return true;
        }
        
        // 営業担当者または部長のみ申請可能
        if (!requesterRole.isSalesRepresentative() && !requesterRole.isDepartmentManager()) {
            throw new IllegalArgumentException("営業担当者または部長のみが担当者変更を申請できます");
        }
        
        // 要件10.2: 営業担当者は自分が担当する顧客の担当者変更を申請できる
        if (requesterRole.isSalesRepresentative() && !requesterRole.isDepartmentManager()) {
            if (!customer.isAssignedTo(requesterId)) {
                throw new IllegalArgumentException("自分が担当する顧客の担当者変更のみ申請できます");
            }
        }
        
        // 部長は同じ営業部の顧客の担当者変更を申請できる
        if (requesterRole.isDepartmentManager()) {
            if (requesterDepartmentId != null && !customer.belongsToDepartment(requesterDepartmentId)) {
                throw new IllegalArgumentException("同じ営業部の顧客の担当者変更のみ申請できます");
            }
        }
        
        // 承認待ち状態の顧客は編集不可（要件3.16）
        if (customer.isPendingApproval()) {
            throw new IllegalArgumentException("承認待ちの顧客は担当者変更できません");
        }
        
        return true;
    }
    
    /**
     * 担当者変更の直接実行権限をチェック
     * 
     * @param executorId 実行者ID
     * @param executorRole 実行者の役割
     * @param executorDepartmentId 実行者の営業部ID
     * @param customer 対象顧客
     * @param changeRequest 変更申請
     * @return 直接実行可能な場合true
     */
    public boolean canDirectlyExecuteAssignmentChange(UserId executorId, Role executorRole,
                                                    SalesDepartmentId executorDepartmentId,
                                                    Customer customer,
                                                    AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(executorId, "実行者IDは必須です");
        Objects.requireNonNull(executorRole, "実行者の役割は必須です");
        Objects.requireNonNull(customer, "対象顧客は必須です");
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // 要件10.6: 情報システム部は営業部をまたいだ担当者変更を直接実行できる
        if (executorRole.isSystemAdministrator()) {
            return true;
        }
        
        // 要件10.7: 部長は同じ営業部内の担当者変更を直接実行できる
        if (executorRole.isDepartmentManager() && changeRequest.getChangeType().isWithinDepartment()) {
            if (executorDepartmentId != null && customer.belongsToDepartment(executorDepartmentId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 担当者変更申請の承認権限をチェック
     * 
     * @param approverId 承認者ID
     * @param approverRole 承認者の役割
     * @param approverDepartmentId 承認者の営業部ID
     * @param changeRequest 変更申請
     * @return 承認可能な場合true
     */
    public boolean canApproveAssignmentChange(UserId approverId, Role approverRole,
                                            SalesDepartmentId approverDepartmentId,
                                            AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(approverId, "承認者IDは必須です");
        Objects.requireNonNull(approverRole, "承認者の役割は必須です");
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // システム管理者は常に承認可能
        if (approverRole.isSystemAdministrator()) {
            return true;
        }
        
        // 部長のみ承認可能
        if (!approverRole.isDepartmentManager()) {
            return false;
        }
        
        // 要件3.11: 申請者本人が部長の場合でも、自分では承認できず他の部長の承認が必要
        if (changeRequest.getRequesterId().equals(approverId)) {
            return false;
        }
        
        // 承認が必要な営業部に所属している部長のみ承認可能
        SalesDepartmentId[] requiredDepartments = changeRequest.getRequiredApprovalDepartments();
        for (SalesDepartmentId departmentId : requiredDepartments) {
            if (departmentId.equals(approverDepartmentId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 担当者変更申請が有効かどうかをチェック
     * 
     * @param changeRequest 変更申請
     * @param customer 対象顧客
     * @return 有効な場合true
     */
    public boolean isValidAssignmentChangeRequest(AssignmentChangeRequest changeRequest, Customer customer) {
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        Objects.requireNonNull(customer, "対象顧客は必須です");
        
        // 現在の担当者が申請時の担当者と一致しているかチェック
        if (!customer.getAssignedSalesRep().equals(changeRequest.getCurrentAssignment())) {
            return false;
        }
        
        // 顧客が編集可能な状態かチェック
        if (!customer.isEditable()) {
            return false;
        }
        
        return true;
    }
}