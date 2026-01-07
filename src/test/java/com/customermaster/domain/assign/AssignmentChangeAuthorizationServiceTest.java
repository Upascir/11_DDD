package com.customermaster.domain.assign;

import com.customermaster.domain.customer.*;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.User;
import com.customermaster.domain.user.UserName;
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
        User admin = User.create(
            UserId.generate(), 
            UserName.of("システム管理者"), 
            Role.SYSTEM_ADMINISTRATOR, 
            null
        );
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            admin, customer));
    }

    @Test
    @DisplayName("営業担当者は自分が担当する法人の担当者変更を申請できる")
    void salesRepresentativeCanRequestForOwnCustomer() {
        // Given
        User salesRep = User.create(
            salesRepId, 
            UserName.of("営業担当者"), 
            Role.SALES_REPRESENTATIVE, 
            departmentId
        );
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            salesRep, customer));
    }

    @Test
    @DisplayName("営業担当者は他人が担当する法人の担当者変更を申請できない")
    void salesRepresentativeCannotRequestForOthersCustomer() {
        // Given
        User otherSalesRep = User.create(
            UserId.generate(), 
            UserName.of("他の営業担当者"), 
            Role.SALES_REPRESENTATIVE, 
            departmentId
        );
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeRequest(
            otherSalesRep, customer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("自分が担当する法人の担当者変更のみ申請できます");
    }

    @Test
    @DisplayName("部長は同じ営業部の法人の担当者変更を申請できる")
    void departmentManagerCanRequestForSameDepartmentCustomer() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("部長"), 
            Role.DEPARTMENT_MANAGER, 
            departmentId
        );
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeRequest(
            manager, customer));
    }

    @Test
    @DisplayName("部長は他の営業部の法人の担当者変更を申請できない")
    void departmentManagerCannotRequestForOtherDepartmentCustomer() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("他部門の部長"), 
            Role.DEPARTMENT_MANAGER, 
            SalesDepartmentId.generate()
        );
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeRequest(
            manager, customer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("同じ営業部の法人の担当者変更のみ申請できます");
    }

    @Test
    @DisplayName("承認待ち状態の法人は担当者変更申請できない")
    void cannotRequestForPendingApprovalCustomer() {
        // Given
        customer.requestApproval(); // 承認待ち状態にする
        User requester = User.create(
            salesRepId, // 担当者として設定されているユーザーIDを使用
            UserName.of("申請者"), 
            Role.SALES_REPRESENTATIVE, 
            departmentId
        );
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeRequest(
            requester, customer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("承認待ちの法人は担当者変更できません");
    }

    @Test
    @DisplayName("システム管理者は担当者変更を直接実行できる")
    void systemAdministratorCanDirectlyExecuteAssignmentChange() {
        // Given
        User admin = User.create(
            UserId.generate(), 
            UserName.of("システム管理者"), 
            Role.SYSTEM_ADMINISTRATOR, 
            null
        );
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            admin.getId(), UserId.generate(), SalesDepartmentId.generate(), "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            admin, customer, changeRequest));
    }

    @Test
    @DisplayName("部長は同じ営業部内の担当者変更を直接実行できる")
    void departmentManagerCanDirectlyExecuteWithinDepartmentChange() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("部長"), 
            Role.DEPARTMENT_MANAGER, 
            departmentId
        );
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            manager.getId(), UserId.generate(), departmentId, "理由"); // 営業部内変更
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            manager, customer, changeRequest));
    }

    @Test
    @DisplayName("部長は営業部間の担当者変更を直接実行できない")
    void departmentManagerCannotDirectlyExecuteCrossDepartmentChange() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("部長"), 
            Role.DEPARTMENT_MANAGER, 
            departmentId
        );
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            manager.getId(), UserId.generate(), SalesDepartmentId.generate(), "理由"); // 営業部間変更
        
        // When & Then
        assertThatThrownBy(() -> service.validateDirectAssignmentChangeExecution(
            manager, customer, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("担当者変更を直接実行する権限がありません");
    }

    @Test
    @DisplayName("部長は担当者変更申請を承認できる")
    void departmentManagerCanApproveAssignmentChange() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("部長"), 
            Role.DEPARTMENT_MANAGER, 
            departmentId
        );
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatNoException().isThrownBy(() -> service.validateAssignmentChangeApproval(
            manager, changeRequest));
    }

    @Test
    @DisplayName("申請者本人は自分の申請を承認できない")
    void requesterCannotApproveSelfRequest() {
        // Given
        UserId requesterId = UserId.generate();
        User requester = User.create(
            requesterId, 
            UserName.of("申請者兼部長"), 
            Role.DEPARTMENT_MANAGER, 
            departmentId
        );
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            requester, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("申請者本人は承認できません");
    }

    @Test
    @DisplayName("他の営業部の部長は承認できない")
    void otherDepartmentManagerCannotApprove() {
        // Given
        User manager = User.create(
            UserId.generate(), 
            UserName.of("他部門の部長"), 
            Role.DEPARTMENT_MANAGER, 
            SalesDepartmentId.generate()
        );
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            manager, changeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("この申請を承認する権限がありません");
    }

    @Test
    @DisplayName("営業担当者は承認できない")
    void salesRepresentativeCannotApprove() {
        // Given
        User salesRep = User.create(
            UserId.generate(), 
            UserName.of("営業担当者"), 
            Role.SALES_REPRESENTATIVE, 
            departmentId
        );
        UserId requesterId = UserId.generate();
        AssignmentChangeRequest changeRequest = customer.createAssignmentChangeRequest(
            requesterId, UserId.generate(), departmentId, "理由");
        
        // When & Then
        assertThatThrownBy(() -> service.validateAssignmentChangeApproval(
            salesRep, changeRequest))
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