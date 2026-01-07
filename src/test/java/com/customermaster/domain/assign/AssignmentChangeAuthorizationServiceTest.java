package com.customermaster.domain.assign;

import com.customermaster.domain.customer.*;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.Role;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * AssignmentChangeAuthorizationService の単体テスト
 */
class AssignmentChangeAuthorizationServiceTest {

    private AssignmentChangeAuthorizationService service;
    private Customer customer;
    private UserId salesRepId;
    private SalesDepartmentId departmentId;

    @BeforeEach
    void setUp() {
        service = new AssignmentChangeAuthorizationService();
        
        // テスト用の法人を作成
        salesRepId = UserId.generate();
        departmentId = SalesDepartmentId.generate();
        
        customer = Customer.create(
            CustomerId.generate(),
            createBasicInfo(),
            createAddress(),
            createContactInfo(),
            createBankAccount(),
            AssignedSalesRepresentative.assignNow(salesRepId, departmentId),
            createCreditInfo()
        );
    }

    @Test
    @DisplayName("システム管理者は常に担当者変更申請できる")
    void systemAdministratorCanAlwaysRequestAssignmentChange() {
        // Given
        UserId adminId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            adminId, UserId.generate(), SalesDepartmentId.generate(), "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            adminId, Role.SYSTEM_ADMINISTRATOR, null, customer, changeRequest));
    }

    @Test
    @DisplayName("営業担当者は自分が担当する法人の担当者変更を申請できる")
    void salesRepresentativeCanRequestForOwnCustomer() {
        // Given
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            salesRepId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            salesRepId, Role.SALES_REPRESENTATIVE, departmentId, customer, changeRequest));
    }

    @Test
    @DisplayName("営業担当者は他人が担当する法人の担当者変更を申請できない")
    void salesRepresentativeCannotRequestForOthersCustomer() {
        // Given
        UserId otherSalesRepId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            otherSalesRepId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeRequest(
            otherSalesRepId, Role.SALES_REPRESENTATIVE, departmentId, customer, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("自分が担当する法人の担当者変更のみ申請できます");
    }

    @Test
    @DisplayName("部長は同じ営業部の法人の担当者変更を申請できる")
    void departmentManagerCanRequestForSameDepartmentCustomer() {
        // Given
        UserId managerId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            managerId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            managerId, Role.DEPARTMENT_MANAGER, departmentId, customer, changeRequest));
    }

    @Test
    @DisplayName("部長は他の営業部の法人の担当者変更を申請できない")
    void departmentManagerCannotRequestForOtherDepartmentCustomer() {
        // Given
        UserId managerId = UserId.generate();
        SalesDepartmentId otherDepartmentId = SalesDepartmentId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            managerId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeRequest(
            managerId, Role.DEPARTMENT_MANAGER, otherDepartmentId, customer, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("同じ営業部の法人の担当者変更のみ申請できます");
    }

    @Test
    @DisplayName("承認待ち状態の法人は担当者変更申請できない")
    void cannotRequestForPendingApprovalCustomer() {
        // Given
        customer.requestApproval(); // 承認待ち状態にする
        UserId requesterId = UserId.generate();
        
        // When & Then
        assertThatThrownBy(() -> customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("現在の状態では担当者変更申請できません");
    }

    @Test
    @DisplayName("システム管理者は担当者変更を直接実行できる")
    void systemAdministratorCanDirectlyExecuteAssignmentChange() {
        // Given
        UserId adminId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            adminId, UserId.generate(), SalesDepartmentId.generate(), "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            adminId, Role.SYSTEM_ADMINISTRATOR, null, customer, changeRequest));
    }

    @Test
    @DisplayName("部長は同じ営業部内の担当者変更を直接実行できる")
    void departmentManagerCanDirectlyExecuteWithinDepartmentChange() {
        // Given
        UserId managerId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            managerId, UserId.generate(), departmentId, "理由"); // 営業部内変更
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            managerId, Role.DEPARTMENT_MANAGER, departmentId, customer, changeRequest));
    }

    @Test
    @DisplayName("部長は営業部間の担当者変更を直接実行できない")
    void departmentManagerCannotDirectlyExecuteCrossDepartmentChange() {
        // Given
        UserId managerId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            managerId, UserId.generate(), SalesDepartmentId.generate(), "理由"); // 営業部間変更
        
        // When & Then
        assertThatThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            managerId, Role.DEPARTMENT_MANAGER, departmentId, customer, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("担当者変更を直接実行する権限がありません");
    }

    @Test
    @DisplayName("部長は担当者変更申請を承認できる")
    void departmentManagerCanApproveAssignmentChange() {
        // Given
        UserId managerId = UserId.generate();
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeApproval(
            managerId, Role.DEPARTMENT_MANAGER, departmentId, changeRequest));
    }

    @Test
    @DisplayName("申請者本人は自分の申請を承認できない")
    void requesterCannotApproveSelfRequest() {
        // Given
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            requesterId, Role.DEPARTMENT_MANAGER, departmentId, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("申請者本人は承認できません");
    }

    @Test
    @DisplayName("他の営業部の部長は承認できない")
    void otherDepartmentManagerCannotApprove() {
        // Given
        UserId managerId = UserId.generate();
        UserId requesterId = UserId.generate();
        SalesDepartmentId otherDepartmentId = SalesDepartmentId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            managerId, Role.DEPARTMENT_MANAGER, otherDepartmentId, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("この申請を承認する権限がありません");
    }

    @Test
    @DisplayName("営業担当者は承認できない")
    void salesRepresentativeCannotApprove() {
        // Given
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            UserId.generate(), Role.SALES_REPRESENTATIVE, departmentId, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("部長のみが担当者変更申請を承認できます");
    }

    @Test
    @DisplayName("有効な担当者変更申請かどうかを判定できる")
    void canValidateAssignmentChangeRequest() {
        // Given
        AssignmentChangeRequest validRequest = customer.createAssignmentChangeRequest(
            salesRepId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThat(service.isValidAssignmentChangeRequest(validRequest, customer))
            .isTrue();
    }

    @Test
    @DisplayName("担当者が変更された後の申請は無効")
    void invalidRequestAfterAssignmentChange() {
        // Given
        AssignmentChangeRequest request = customer.createAssignmentChangeRequest(
            salesRepId, UserId.generate(), departmentId, "理由");
        
        // 担当者を変更
        customer.assignSalesRepresentative(AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId));
        
        // When & Then
        assertThat(service.isValidAssignmentChangeRequest(request, customer))
            .isFalse();
    }

    // テストヘルパーメソッド

    private CustomerBasicInfo createBasicInfo() {
        return CustomerBasicInfo.of(
            CustomerName.of("株式会社テスト"),
            "カブシキガイシャテスト",
            "情報通信業",
            LocalDate.of(2000, 1, 1),
            100,
            new BigDecimal("10000000"),
            new BigDecimal("100000000")
        );
    }

    private Address createAddress() {
        return Address.of("100-0001", "東京都", "千代田区", "丸の内1-1-1", "テストビル");
    }

    private ContactInfo createContactInfo() {
        return ContactInfo.of("03-1234-5678", "03-1234-5679", "info@test.com");
    }

    private BankAccount createBankAccount() {
        return BankAccount.of("0001", "三菱UFJ銀行", "001", "本店",
                            BankAccount.AccountType.ORDINARY, "1234567", "株式会社テスト");
    }

    private CreditInfo createCreditInfo() {
        return CreditInfo.createMinimal(CreditInfo.CreditRank.A);
    }
}