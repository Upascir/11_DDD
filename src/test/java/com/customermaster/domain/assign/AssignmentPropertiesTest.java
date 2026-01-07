package com.customermaster.domain.assign;

import com.customermaster.domain.customer.Customer;
import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.customer.CustomerBasicInfo;
import com.customermaster.domain.customer.CustomerName;
import com.customermaster.domain.customer.CreditInfo;
import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.User;
import com.customermaster.domain.user.UserName;
import com.customermaster.domain.user.Role;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * 担当者割り当て管理のプロパティベーステスト
 * 
 * プロパティ5: 担当者割り当ての整合性
 * 検証要件: 2.9, 10.1
 */
class AssignmentPropertiesTest {

    /**
     * プロパティ5: 担当営業者の必須性
     * 全ての法人に担当営業者が設定される（要件2.9）
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 担当営業者の必須性")
    void customerMustHaveAssignedSalesRepresentative(@ForAll("validCustomerData") CustomerTestData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId(),
            customerData.basicInfo(),
            customerData.headquartersAddress(),
            customerData.representativeContact(),
            customerData.mainBankAccount(),
            customerData.assignedSalesRep(),
            customerData.creditInfo()
        );
        
        // Then - 要件2.9: 法人情報には担当営業者が紐づけられる
        assertThat(customer.getAssignedSalesRep()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getSalesRepresentativeId()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getDepartmentId()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getAssignedAt()).isNotNull();
    }

    /**
     * プロパティ5: 担当者変更申請の作成整合性
     * 有効な担当者変更申請が作成できる（要件10.1）
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 担当者変更申請の作成整合性")
    void canCreateValidAssignmentChangeRequest(@ForAll("validAssignmentChangeData") AssignmentChangeTestData changeData) {
        // When
        AssignmentChangeRequest changeRequest = AssignmentChangeRequest.create(
            CustomerId.generate(),
            changeData.requesterId(),
            changeData.currentAssignment(),
            changeData.newSalesRepId(),
            changeData.newDepartmentId(),
            changeData.reason()
        );
        
        // Then - 要件10.1: 担当者変更申請の基本機能
        assertThat(changeRequest.getRequesterId()).isEqualTo(changeData.requesterId());
        assertThat(changeRequest.getCurrentAssignment()).isEqualTo(changeData.currentAssignment());
        assertThat(changeRequest.getNewAssignment().getSalesRepresentativeId()).isEqualTo(changeData.newSalesRepId());
        assertThat(changeRequest.getNewAssignment().getDepartmentId()).isEqualTo(changeData.newDepartmentId());
        assertThat(changeRequest.getReason()).isEqualTo(changeData.reason());
        assertThat(changeRequest.getRequestedAt()).isNotNull();
        assertThat(changeRequest.getChangeType()).isNotNull();
    }

    /**
     * プロパティ5: 営業部内変更と営業部間変更の判定
     * 変更種類が正しく判定される
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 営業部内変更と営業部間変更の判定")
    void changeTypeIsCorrectlyDetermined(@ForAll("validAssignmentChangeData") AssignmentChangeTestData changeData) {
        // When
        AssignmentChangeRequest changeRequest = AssignmentChangeRequest.create(
            CustomerId.generate(),
            changeData.requesterId(),
            changeData.currentAssignment(),
            changeData.newSalesRepId(),
            changeData.newDepartmentId(),
            changeData.reason()
        );
        
        // Then - 変更種類が正しく判定される
        boolean isCrossDepartment = !changeData.currentAssignment().getDepartmentId()
                                   .equals(changeData.newDepartmentId());
        
        if (isCrossDepartment) {
            assertThat(changeRequest.getChangeType()).isEqualTo(AssignmentChangeType.CROSS_DEPARTMENT);
            assertThat(changeRequest.getChangeType().isCrossDepartment()).isTrue();
        } else {
            assertThat(changeRequest.getChangeType()).isEqualTo(AssignmentChangeType.WITHIN_DEPARTMENT);
            assertThat(changeRequest.getChangeType().isWithinDepartment()).isTrue();
        }
    }

    /**
     * プロパティ5: 承認が必要な営業部の特定
     * 変更種類に応じて承認が必要な営業部が正しく特定される
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 承認が必要な営業部の特定")
    void requiredApprovalDepartmentsAreCorrect(@ForAll("validAssignmentChangeData") AssignmentChangeTestData changeData) {
        // When
        AssignmentChangeRequest changeRequest = AssignmentChangeRequest.create(
            CustomerId.generate(),
            changeData.requesterId(),
            changeData.currentAssignment(),
            changeData.newSalesRepId(),
            changeData.newDepartmentId(),
            changeData.reason()
        );
        
        SalesDepartmentId[] requiredDepartments = changeRequest.getRequiredApprovalDepartments();
        
        // Then - 承認が必要な営業部が正しく特定される
        if (changeRequest.getChangeType().isCrossDepartment()) {
            // 営業部間変更の場合、両方の営業部の承認が必要
            assertThat(requiredDepartments).hasSize(2);
            assertThat(requiredDepartments).contains(
                changeData.currentAssignment().getDepartmentId(),
                changeData.newDepartmentId()
            );
        } else {
            // 営業部内変更の場合、現在の営業部の承認のみ必要
            assertThat(requiredDepartments).hasSize(1);
            assertThat(requiredDepartments[0]).isEqualTo(changeData.currentAssignment().getDepartmentId());
        }
    }

    /**
     * プロパティ5: 担当者変更申請の権限チェック
     * 申請権限が正しくチェックされる
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 担当者変更申請の権限チェック")
    void assignmentChangeRequestAuthorizationIsCorrect(@ForAll("validAuthorizationTestData") AuthorizationTestData authData) {
        // Given
        AssignmentChangeAuthorizationService authService = new AssignmentChangeAuthorizationService();
        
        // システム管理者以外は部門IDが必要
        SalesDepartmentId departmentId = authData.requesterRole().isSystemAdministrator() 
            ? null 
            : (authData.requesterDepartmentId() != null ? authData.requesterDepartmentId() : SalesDepartmentId.generate());
            
        User requester = User.create(
            authData.requesterId(),
            UserName.of("テストユーザー"),
            authData.requesterRole(),
            departmentId
        );
        
        // When & Then
        if (authData.requesterRole().isSystemAdministrator()) {
            // システム管理者は常に申請可能
            assertThatNoException().isThrownBy(() -> authService.validateAssignmentChangeRequest(
                requester,
                authData.customer()
            ));
        } else if (authData.requesterRole().isSalesRepresentative() && !authData.requesterRole().isDepartmentManager()) {
            // 営業担当者は自分が担当する法人のみ申請可能
            if (authData.customer().isAssignedTo(authData.requesterId())) {
                assertThatNoException().isThrownBy(() -> authService.validateAssignmentChangeRequest(
                    requester,
                    authData.customer()
                ));
            } else {
                assertThatThrownBy(() -> authService.validateAssignmentChangeRequest(
                    requester,
                    authData.customer()
                )).isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("自分が担当する法人の担当者変更のみ申請できます");
            }
        }
    }

    /**
     * プロパティ5: 担当者変更の直接実行権限
     * 直接実行権限が正しくチェックされる
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 担当者変更の直接実行権限")
    void directAssignmentChangeAuthorizationIsCorrect(@ForAll("validAuthorizationTestData") AuthorizationTestData authData) {
        // Given
        AssignmentChangeAuthorizationService authService = new AssignmentChangeAuthorizationService();
        
        // システム管理者以外は部門IDが必要
        SalesDepartmentId departmentId = authData.requesterRole().isSystemAdministrator() 
            ? null 
            : (authData.requesterDepartmentId() != null ? authData.requesterDepartmentId() : SalesDepartmentId.generate());
            
        User executor = User.create(
            authData.requesterId(),
            UserName.of("テスト実行者"),
            authData.requesterRole(),
            departmentId
        );
        
        // When & Then
        if (authData.requesterRole().isSystemAdministrator()) {
            // 要件10.6: 情報システム部は営業部をまたいだ担当者変更を直接実行できる
            assertThatNoException().isThrownBy(() -> authService.validateDirectAssignmentChangeExecution(
                executor,
                authData.customer(),
                authData.changeRequest()
            ));
        } else if (authData.requesterRole().isDepartmentManager() && 
                   authData.changeRequest().getChangeType().isWithinDepartment()) {
            // 要件10.7: 部長は同じ営業部内の担当者変更を直接実行できる
            if (departmentId != null && 
                authData.customer().belongsToDepartment(departmentId)) {
                assertThatNoException().isThrownBy(() -> authService.validateDirectAssignmentChangeExecution(
                    executor,
                    authData.customer(),
                    authData.changeRequest()
                ));
            }
        }
    }

    /**
     * プロパティ5: 担当者変更申請の承認権限
     * 承認権限が正しくチェックされる
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 担当者変更申請の承認権限")
    void assignmentChangeApprovalAuthorizationIsCorrect(@ForAll("validApprovalTestData") ApprovalTestData approvalData) {
        // Given
        AssignmentChangeAuthorizationService authService = new AssignmentChangeAuthorizationService();
        
        // システム管理者以外は部門IDが必要
        SalesDepartmentId departmentId = approvalData.approverRole().isSystemAdministrator() 
            ? null 
            : (approvalData.approverDepartmentId() != null ? approvalData.approverDepartmentId() : SalesDepartmentId.generate());
            
        User approver = User.create(
            approvalData.approverId(),
            UserName.of("テスト承認者"),
            approvalData.approverRole(),
            departmentId
        );
        
        // When & Then
        if (approvalData.approverRole().isSystemAdministrator()) {
            // システム管理者は常に承認可能
            assertThatNoException().isThrownBy(() -> authService.validateAssignmentChangeApproval(
                approver,
                approvalData.changeRequest()
            ));
        } else if (approvalData.approverRole().isDepartmentManager()) {
            // 申請者本人が部長の場合でも、自分では承認できない
            if (approvalData.changeRequest().getRequesterId().equals(approvalData.approverId())) {
                assertThatThrownBy(() -> authService.validateAssignmentChangeApproval(
                    approver,
                    approvalData.changeRequest()
                )).isInstanceOf(IllegalArgumentException.class)
                  .hasMessageContaining("申請者本人は承認できません");
            } else {
                // 承認が必要な営業部に所属している部長のみ承認可能
                SalesDepartmentId[] requiredDepartments = approvalData.changeRequest().getRequiredApprovalDepartments();
                boolean canApprove = false;
                for (SalesDepartmentId requiredDepartmentId : requiredDepartments) {
                    if (requiredDepartmentId.equals(departmentId)) {
                        canApprove = true;
                        break;
                    }
                }
                if (canApprove) {
                    assertThatNoException().isThrownBy(() -> authService.validateAssignmentChangeApproval(
                        approver,
                        approvalData.changeRequest()
                    ));
                } else {
                    assertThatThrownBy(() -> authService.validateAssignmentChangeApproval(
                        approver,
                        approvalData.changeRequest()
                    )).isInstanceOf(IllegalArgumentException.class)
                      .hasMessageContaining("この申請を承認する権限がありません");
                }
            }
        } else {
            // 部長以外は承認不可
            assertThatThrownBy(() -> authService.validateAssignmentChangeApproval(
                approver,
                approvalData.changeRequest()
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("部長のみが担当者変更申請を承認できます");
        }
    }

    /**
     * プロパティ5: 同じ担当者への変更は不可
     * 同じ担当者への変更申請は作成できない
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 同じ担当者への変更は不可")
    void cannotChangeToSameSalesRepresentative(@ForAll("validAssignedSalesRep") AssignedSalesRepresentative currentAssignment,
                                               @ForAll("validUserIds") UserId requesterId,
                                               @ForAll("validReasons") String reason) {
        // When & Then - 同じ担当者への変更は不可
        assertThatThrownBy(() -> AssignmentChangeRequest.create(
            CustomerId.generate(),
            requesterId,
            currentAssignment,
            currentAssignment.getSalesRepresentativeId(), // 同じ担当者ID
            currentAssignment.getDepartmentId(),
            reason
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("同じ担当者への変更はできません");
    }

    /**
     * プロパティ5: 変更理由の必須性
     * 変更理由が空の場合は申請作成できない
     */
    @Property
    @Label("Feature: customer-master-system, Property 5: 変更理由の必須性")
    void changeReasonIsRequired(@ForAll("validAssignedSalesRep") AssignedSalesRepresentative currentAssignment,
                               @ForAll("validUserIds") UserId requesterId,
                               @ForAll("validUserIds") UserId newSalesRepId,
                               @ForAll("validDepartmentIds") SalesDepartmentId newDepartmentId,
                               @ForAll("emptyOrBlankStrings") String emptyReason) {
        // When & Then - 変更理由が空の場合は申請作成不可
        assertThatThrownBy(() -> AssignmentChangeRequest.create(
            CustomerId.generate(),
            requesterId,
            currentAssignment,
            newSalesRepId,
            newDepartmentId,
            emptyReason
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("変更理由は必須です");
    }

    // テストデータ生成用のArbitrary

    @Provide
    Arbitrary<CustomerTestData> validCustomerData() {
        return Combinators.combine(
            Arbitraries.create(() -> CustomerId.generate()),
            validBasicInfo(),
            validAddresses(),
            validContactInfos(),
            validBankAccounts(),
            validAssignedSalesRep(),
            validCreditInfos()
        ).as(CustomerTestData::new);
    }

    @Provide
    Arbitrary<AssignmentChangeTestData> validAssignmentChangeData() {
        return Combinators.combine(
            validUserIds(),
            validAssignedSalesRep(),
            validUserIds(),
            validDepartmentIds(),
            validReasons()
        ).as(AssignmentChangeTestData::new);
    }

    @Provide
    Arbitrary<AuthorizationTestData> validAuthorizationTestData() {
        return Combinators.combine(
            validUserIds(),
            validRoles(),
            validDepartmentIds().optional(),
            validCustomers(),
            validAssignmentChangeRequests()
        ).as((requesterId, role, deptId, customer, changeRequest) ->
            new AuthorizationTestData(requesterId, role, deptId.orElse(null), customer, changeRequest));
    }

    @Provide
    Arbitrary<ApprovalTestData> validApprovalTestData() {
        return Combinators.combine(
            validUserIds(),
            validRoles(),
            validDepartmentIds().optional(),
            validAssignmentChangeRequests()
        ).as((approverId, role, deptId, changeRequest) ->
            new ApprovalTestData(approverId, role, deptId.orElse(null), changeRequest));
    }

    @Provide
    Arbitrary<CustomerBasicInfo> validBasicInfo() {
        return Combinators.combine(
            validCustomerNames(),
            validCustomerNameKanas(),
            validIndustryClassifications()
        ).as((name, kana, industry) -> CustomerBasicInfo.of(name, kana, industry));
    }

    @Provide
    Arbitrary<CustomerName> validCustomerNames() {
        return Arbitraries.strings()
            .withCharRange('あ', 'ん')
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(s -> "株式会社" + s)
            .map(CustomerName::of);
    }

    @Provide
    Arbitrary<String> validCustomerNameKanas() {
        return Arbitraries.strings()
            .withCharRange('ア', 'ン')
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(s -> "カブシキガイシャ" + s);
    }

    @Provide
    Arbitrary<String> validIndustryClassifications() {
        return Arbitraries.of(
            "情報通信業", "製造業", "卸売業・小売業", "金融業・保険業",
            "不動産業・物品賃貸業", "建設業", "運輸業・郵便業", "サービス業"
        );
    }

    @Provide
    Arbitrary<Address> validAddresses() {
        return Combinators.combine(
            Arbitraries.strings().numeric().ofLength(3),
            Arbitraries.strings().numeric().ofLength(4),
            Arbitraries.of("東京都", "大阪府", "愛知県", "神奈川県"),
            Arbitraries.of("千代田区", "中央区", "港区", "新宿区"),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(10)
        ).as((zip1, zip2, pref, city, street) ->
            Address.of(zip1 + "-" + zip2, pref, city, street));
    }

    @Provide
    Arbitrary<ContactInfo> validContactInfos() {
        return Combinators.combine(
            validPhoneNumbers(),
            validPhoneNumbers().optional(),
            validEmails().optional()
        ).as((phone, fax, email) -> ContactInfo.of(phone, fax.orElse(null), email.orElse(null)));
    }

    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Combinators.combine(
            Arbitraries.of("03", "06", "052", "045"),
            Arbitraries.strings().numeric().ofLength(4),
            Arbitraries.strings().numeric().ofLength(4)
        ).as((area, middle, last) -> area + "-" + middle + "-" + last);
    }

    @Provide
    Arbitrary<String> validEmails() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.of("test.com", "example.com", "company.co.jp")
        ).as((local, domain) -> local + "@" + domain);
    }

    @Provide
    Arbitrary<BankAccount> validBankAccounts() {
        return Combinators.combine(
            Arbitraries.strings().numeric().ofLength(4),
            Arbitraries.of("三菱UFJ銀行", "みずほ銀行", "三井住友銀行"),
            Arbitraries.strings().numeric().ofLength(3),
            Arbitraries.of("本店", "支店", "出張所"),
            Arbitraries.of(BankAccount.AccountType.values()),
            Arbitraries.strings().numeric().ofLength(7),
            validCustomerNames().map(name -> name.getValue())
        ).as(BankAccount::of);
    }

    @Provide
    Arbitrary<AssignedSalesRepresentative> validAssignedSalesRep() {
        return Combinators.combine(
            validUserIds(),
            validDepartmentIds()
        ).as(AssignedSalesRepresentative::assignNow);
    }

    @Provide
    Arbitrary<CreditInfo> validCreditInfos() {
        return Arbitraries.of(CreditInfo.CreditRank.values())
            .map(CreditInfo::createMinimal);
    }

    @Provide
    Arbitrary<UserId> validUserIds() {
        return Arbitraries.create(() -> UserId.generate());
    }

    @Provide
    Arbitrary<SalesDepartmentId> validDepartmentIds() {
        return Arbitraries.create(() -> SalesDepartmentId.generate());
    }

    @Provide
    Arbitrary<Role> validRoles() {
        return Arbitraries.of(Role.values());
    }

    @Provide
    Arbitrary<String> validReasons() {
        return Arbitraries.of(
            "担当者の退職に伴う変更",
            "営業戦略の見直しによる担当変更",
            "法人からの要望による変更",
            "業務効率化のための担当変更",
            "専門性を活かした担当変更"
        );
    }

    @Provide
    Arbitrary<String> emptyOrBlankStrings() {
        return Arbitraries.of("", "   ", "\t", "\n", "  \t  \n  ");
    }

    @Provide
    Arbitrary<Customer> validCustomers() {
        return validCustomerData().map(data -> Customer.create(
            data.customerId(),
            data.basicInfo(),
            data.headquartersAddress(),
            data.representativeContact(),
            data.mainBankAccount(),
            data.assignedSalesRep(),
            data.creditInfo()
        ));
    }

    @Provide
    Arbitrary<AssignmentChangeRequest> validAssignmentChangeRequests() {
        return validAssignmentChangeData().map(data -> AssignmentChangeRequest.create(
            CustomerId.generate(),
            data.requesterId(),
            data.currentAssignment(),
            data.newSalesRepId(),
            data.newDepartmentId(),
            data.reason()
        ));
    }

    // テストデータ用のレコード
    private record CustomerTestData(
        CustomerId customerId,
        CustomerBasicInfo basicInfo,
        Address headquartersAddress,
        ContactInfo representativeContact,
        BankAccount mainBankAccount,
        AssignedSalesRepresentative assignedSalesRep,
        CreditInfo creditInfo
    ) {}

    private record AssignmentChangeTestData(
        UserId requesterId,
        AssignedSalesRepresentative currentAssignment,
        UserId newSalesRepId,
        SalesDepartmentId newDepartmentId,
        String reason
    ) {}

    private record AuthorizationTestData(
        UserId requesterId,
        Role requesterRole,
        SalesDepartmentId requesterDepartmentId,
        Customer customer,
        AssignmentChangeRequest changeRequest
    ) {}

    private record ApprovalTestData(
        UserId approverId,
        Role approverRole,
        SalesDepartmentId approverDepartmentId,
        AssignmentChangeRequest changeRequest
    ) {}
}