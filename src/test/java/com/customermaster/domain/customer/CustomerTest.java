package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Customer集約の単体テスト
 */
class CustomerTest {

    @Test
    @DisplayName("新規顧客を作成できる")
    void canCreateNewCustomer() {
        // Given
        CustomerId customerId = CustomerId.generate();
        CustomerBasicInfo basicInfo = createBasicInfo();
        Address headquartersAddress = createAddress();
        ContactInfo representativeContact = createContactInfo();
        BankAccount mainBankAccount = createBankAccount();
        AssignedSalesRepresentative assignedSalesRep = createAssignedSalesRep();
        CreditInfo creditInfo = createCreditInfo();
        
        // When
        Customer customer = Customer.create(customerId, basicInfo, headquartersAddress,
                                          representativeContact, mainBankAccount, 
                                          assignedSalesRep, creditInfo);
        
        // Then
        assertThat(customer.getId()).isEqualTo(customerId);
        assertThat(customer.getBasicInfo()).isEqualTo(basicInfo);
        assertThat(customer.getHeadquartersAddress()).isEqualTo(headquartersAddress);
        assertThat(customer.getRepresentativeContact()).isEqualTo(representativeContact);
        assertThat(customer.getMainBankAccount()).isEqualTo(mainBankAccount);
        assertThat(customer.getAssignedSalesRep()).isEqualTo(assignedSalesRep);
        assertThat(customer.getCreditInfo()).isEqualTo(creditInfo);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.DRAFT);
        assertThat(customer.getBranchAddresses()).isEmpty();
        assertThat(customer.getContactPersons()).isEmpty();
        assertThat(customer.getSubBankAccounts()).isEmpty();
    }

    @Test
    @DisplayName("支店住所を追加できる")
    void canAddBranchAddress() {
        // Given
        Customer customer = createCustomer();
        Address branchAddress = Address.of("100-0001", "東京都", "千代田区", "千代田1-1-1");
        
        // When
        customer.addBranchAddress(branchAddress);
        
        // Then
        assertThat(customer.getBranchAddresses()).hasSize(1);
        assertThat(customer.getBranchAddresses().get(0)).isEqualTo(branchAddress);
    }

    @Test
    @DisplayName("支店住所を削除できる")
    void canRemoveBranchAddress() {
        // Given
        Customer customer = createCustomer();
        Address branchAddress = Address.of("100-0001", "東京都", "千代田区", "千代田1-1-1");
        customer.addBranchAddress(branchAddress);
        
        // When
        customer.removeBranchAddress(0);
        
        // Then
        assertThat(customer.getBranchAddresses()).isEmpty();
    }

    @Test
    @DisplayName("担当者連絡先を追加できる")
    void canAddContactPerson() {
        // Given
        Customer customer = createCustomer();
        ContactInfo contactPerson = ContactInfo.of("03-1234-5678", "03-1234-5679", "contact@example.com");
        
        // When
        customer.addContactPerson(contactPerson);
        
        // Then
        assertThat(customer.getContactPersons()).hasSize(1);
        assertThat(customer.getContactPersons().get(0)).isEqualTo(contactPerson);
    }

    @Test
    @DisplayName("サブ口座を追加できる")
    void canAddSubBankAccount() {
        // Given
        Customer customer = createCustomer();
        BankAccount subAccount = BankAccount.of("0001", "みずほ銀行", "001", "本店",
                                              BankAccount.AccountType.ORDINARY, "1234567", "株式会社テスト");
        
        // When
        customer.addSubBankAccount(subAccount);
        
        // Then
        assertThat(customer.getSubBankAccounts()).hasSize(1);
        assertThat(customer.getSubBankAccounts().get(0)).isEqualTo(subAccount);
    }

    @Test
    @DisplayName("サブ口座は最大3件まで追加できる")
    void canAddMaxThreeSubBankAccounts() {
        // Given
        Customer customer = createCustomer();
        
        // When & Then - 3件まで追加可能
        for (int i = 1; i <= 3; i++) {
            BankAccount subAccount = BankAccount.of("000" + i, "銀行" + i, "001", "本店",
                                                  BankAccount.AccountType.ORDINARY, "123456" + i, "株式会社テスト");
            customer.addSubBankAccount(subAccount);
        }
        assertThat(customer.getSubBankAccounts()).hasSize(3);
        
        // 4件目は例外
        BankAccount fourthAccount = BankAccount.of("0004", "銀行4", "001", "本店",
                                                 BankAccount.AccountType.ORDINARY, "1234564", "株式会社テスト");
        assertThatThrownBy(() -> customer.addSubBankAccount(fourthAccount))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("サブ口座は最大3件まで登録できます");
    }

    @Test
    @DisplayName("担当営業者を変更できる")
    void canAssignSalesRepresentative() {
        // Given
        Customer customer = createCustomer();
        AssignedSalesRepresentative newAssignedSalesRep = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());
        
        // When
        customer.assignSalesRepresentative(newAssignedSalesRep);
        
        // Then
        assertThat(customer.getAssignedSalesRep()).isEqualTo(newAssignedSalesRep);
    }

    @Test
    @DisplayName("承認申請ができる")
    void canRequestApproval() {
        // Given
        Customer customer = createCustomer();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.DRAFT);
        
        // When
        customer.requestApproval();
        
        // Then
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("承認待ち状態から承認できる")
    void canApproveFromPendingStatus() {
        // Given
        Customer customer = createCustomer();
        customer.requestApproval();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.PENDING_APPROVAL);
        
        // When
        customer.approve();
        
        // Then
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.APPROVED);
    }

    @Test
    @DisplayName("承認待ち状態から却下できる")
    void canRejectFromPendingStatus() {
        // Given
        Customer customer = createCustomer();
        customer.requestApproval();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.PENDING_APPROVAL);
        
        // When
        customer.reject();
        
        // Then
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.REJECTED);
    }

    @Test
    @DisplayName("却下状態から再度承認申請できる")
    void canRequestApprovalAfterRejection() {
        // Given
        Customer customer = createCustomer();
        customer.requestApproval();
        customer.reject();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.REJECTED);
        
        // When
        customer.requestApproval();
        
        // Then
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("承認済み状態では承認申請できない")
    void cannotRequestApprovalWhenApproved() {
        // Given
        Customer customer = createCustomer();
        customer.requestApproval();
        customer.approve();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.APPROVED);
        
        // When & Then
        assertThatThrownBy(() -> customer.requestApproval())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("現在の状態では承認申請できません");
    }

    @Test
    @DisplayName("下書き状態では承認できない")
    void cannotApproveWhenDraft() {
        // Given
        Customer customer = createCustomer();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.DRAFT);
        
        // When & Then
        assertThatThrownBy(() -> customer.approve())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("現在の状態では承認できません");
    }

    @Test
    @DisplayName("支払い信頼性を判定できる")
    void canDeterminePaymentReliability() {
        // Given - 信用度BBBの顧客（信頼できる）
        Customer reliableCustomer = createCustomerWithCreditRank(CreditInfo.CreditRank.BBB);
        
        // Given - 信用度Cの顧客（信頼できない）
        Customer unreliableCustomer = createCustomerWithCreditRank(CreditInfo.CreditRank.C);
        
        // When & Then
        assertThat(reliableCustomer.isPaymentReliable()).isTrue();
        assertThat(unreliableCustomer.isPaymentReliable()).isFalse();
    }

    @Test
    @DisplayName("担当営業者の判定ができる")
    void canCheckAssignedSalesRepresentative() {
        // Given
        UserId salesRepId = UserId.generate();
        Customer customer = createCustomerWithSalesRep(salesRepId);
        
        // When & Then
        assertThat(customer.isAssignedTo(salesRepId)).isTrue();
        assertThat(customer.isAssignedTo(UserId.generate())).isFalse();
    }

    @Test
    @DisplayName("営業部所属の判定ができる")
    void canCheckDepartmentBelonging() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        Customer customer = createCustomerWithDepartment(departmentId);
        
        // When & Then
        assertThat(customer.belongsToDepartment(departmentId)).isTrue();
        assertThat(customer.belongsToDepartment(SalesDepartmentId.generate())).isFalse();
    }

    // テストヘルパーメソッド

    private Customer createCustomer() {
        return Customer.create(
            CustomerId.generate(),
            createBasicInfo(),
            createAddress(),
            createContactInfo(),
            createBankAccount(),
            createAssignedSalesRep(),
            createCreditInfo()
        );
    }

    private Customer createCustomerWithCreditRank(CreditInfo.CreditRank creditRank) {
        return Customer.create(
            CustomerId.generate(),
            createBasicInfo(),
            createAddress(),
            createContactInfo(),
            createBankAccount(),
            createAssignedSalesRep(),
            CreditInfo.createMinimal(creditRank)
        );
    }

    private Customer createCustomerWithSalesRep(UserId salesRepId) {
        return Customer.create(
            CustomerId.generate(),
            createBasicInfo(),
            createAddress(),
            createContactInfo(),
            createBankAccount(),
            AssignedSalesRepresentative.assignNow(salesRepId, SalesDepartmentId.generate()),
            createCreditInfo()
        );
    }

    private Customer createCustomerWithDepartment(SalesDepartmentId departmentId) {
        return Customer.create(
            CustomerId.generate(),
            createBasicInfo(),
            createAddress(),
            createContactInfo(),
            createBankAccount(),
            AssignedSalesRepresentative.assignNow(UserId.generate(), departmentId),
            createCreditInfo()
        );
    }

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

    private AssignedSalesRepresentative createAssignedSalesRep() {
        return AssignedSalesRepresentative.assignNow(UserId.generate(), SalesDepartmentId.generate());
    }

    private CreditInfo createCreditInfo() {
        return CreditInfo.createMinimal(CreditInfo.CreditRank.A);
    }
}