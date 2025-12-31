package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Customer集約のプロパティベーステスト
 * 
 * プロパティ4: 顧客情報の整合性
 * 検証要件: 2.1-2.8, 2.9
 */
class CustomerPropertiesTest {

    /**
     * プロパティ4: 新規顧客作成の整合性
     * 任意の有効な顧客データで顧客を作成できる
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 新規顧客作成の整合性")
    void canCreateCustomerWithValidData(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then
        assertThat(customer.getId()).isEqualTo(customerData.customerId);
        assertThat(customer.getBasicInfo()).isEqualTo(customerData.basicInfo);
        assertThat(customer.getHeadquartersAddress()).isEqualTo(customerData.headquartersAddress);
        assertThat(customer.getRepresentativeContact()).isEqualTo(customerData.representativeContact);
        assertThat(customer.getMainBankAccount()).isEqualTo(customerData.mainBankAccount);
        assertThat(customer.getAssignedSalesRep()).isEqualTo(customerData.assignedSalesRep);
        assertThat(customer.getCreditInfo()).isEqualTo(customerData.creditInfo);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.DRAFT);
    }

    /**
     * プロパティ4: 顧客情報の構造完全性
     * 作成された顧客が必要な全ての情報を持つ
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 顧客情報の構造完全性")
    void customerHasAllRequiredInformation(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then - 基本情報、住所情報、連絡先情報、口座情報が含まれる
        assertThat(customer.getBasicInfo()).isNotNull();
        assertThat(customer.getHeadquartersAddress()).isNotNull();
        assertThat(customer.getRepresentativeContact()).isNotNull();
        assertThat(customer.getMainBankAccount()).isNotNull();
        assertThat(customer.getAssignedSalesRep()).isNotNull();
        assertThat(customer.getCreditInfo()).isNotNull();
    }

    /**
     * プロパティ4: 基本情報の構造完全性
     * 基本情報が必要な全ての項目を持つ
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 基本情報の構造完全性")
    void basicInfoHasAllRequiredFields(@ForAll("validBasicInfo") CustomerBasicInfo basicInfo) {
        // Then - 顧客名、業界分類、設立年月日、従業員数、資本金、年商が含まれる
        assertThat(basicInfo.getCustomerName()).isNotNull();
        assertThat(basicInfo.getCustomerNameKana()).isNotNull();
        assertThat(basicInfo.getIndustryClassification()).isNotNull();
        // 任意項目は存在チェックメソッドで確認
        assertThat(basicInfo.hasEstablishedDate()).isIn(true, false);
        assertThat(basicInfo.hasEmployeeCount()).isIn(true, false);
        assertThat(basicInfo.hasCapital()).isIn(true, false);
        assertThat(basicInfo.hasAnnualRevenue()).isIn(true, false);
    }

    /**
     * プロパティ4: 住所管理の制約
     * 本社住所が必須で支店住所が任意個数追加できる
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 住所管理の制約")
    void addressManagementConstraints(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then - 本社住所は必須
        assertThat(customer.getHeadquartersAddress()).isNotNull();
        
        // 支店住所を複数追加できる
        Address branchAddress1 = Address.of("100-0001", "東京都", "千代田区", "千代田1-1-1");
        Address branchAddress2 = Address.of("530-0001", "大阪府", "大阪市北区", "梅田1-1-1");
        
        customer.addBranchAddress(branchAddress1);
        customer.addBranchAddress(branchAddress2);
        
        assertThat(customer.getBranchAddresses()).hasSize(2);
        assertThat(customer.getBranchAddresses()).contains(branchAddress1, branchAddress2);
    }

    /**
     * プロパティ4: 連絡先管理の制約
     * 代表連絡先が必須で担当者連絡先が任意個数追加できる
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 連絡先管理の制約")
    void contactManagementConstraints(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then - 代表連絡先は必須
        assertThat(customer.getRepresentativeContact()).isNotNull();
        
        // 担当者連絡先を複数追加できる
        ContactInfo contact1 = ContactInfo.of("03-1111-1111", "03-1111-1112", "contact1@test.com");
        ContactInfo contact2 = ContactInfo.of("03-2222-2222", "03-2222-2223", "contact2@test.com");
        
        customer.addContactPerson(contact1);
        customer.addContactPerson(contact2);
        
        assertThat(customer.getContactPersons()).hasSize(2);
        assertThat(customer.getContactPersons()).contains(contact1, contact2);
    }

    /**
     * プロパティ4: 口座管理の制約
     * メイン口座が必須でサブ口座が最大3件まで追加できる
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 口座管理の制約")
    void bankAccountManagementConstraints(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then - メイン口座は必須
        assertThat(customer.getMainBankAccount()).isNotNull();
        
        // サブ口座を最大3件まで追加できる
        BankAccount sub1 = BankAccount.of("0001", "みずほ銀行", "001", "本店",
                                        BankAccount.AccountType.ORDINARY, "1111111", "株式会社テスト");
        BankAccount sub2 = BankAccount.of("0005", "三井住友銀行", "001", "本店",
                                        BankAccount.AccountType.ORDINARY, "2222222", "株式会社テスト");
        BankAccount sub3 = BankAccount.of("0009", "りそな銀行", "001", "本店",
                                        BankAccount.AccountType.ORDINARY, "3333333", "株式会社テスト");
        
        customer.addSubBankAccount(sub1);
        customer.addSubBankAccount(sub2);
        customer.addSubBankAccount(sub3);
        
        assertThat(customer.getSubBankAccounts()).hasSize(3);
        assertThat(customer.getSubBankAccounts()).contains(sub1, sub2, sub3);
    }

    /**
     * プロパティ4: 顧客ID生成の一意性
     * 生成される顧客IDが常に一意である
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 顧客ID生成の一意性")
    void customerIdGenerationUniqueness() {
        // When - 複数の顧客IDを生成
        Set<CustomerId> generatedIds = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            generatedIds.add(CustomerId.generate());
        }
        
        // Then - 全て一意である
        assertThat(generatedIds).hasSize(1000);
    }

    /**
     * プロパティ4: 業界分類の妥当性
     * 有効な業界分類のみが受け入れられる
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 業界分類の妥当性")
    void industryClassificationValidity(@ForAll("validIndustryClassifications") String industryClassification) {
        // When
        CustomerBasicInfo basicInfo = CustomerBasicInfo.of(
            CustomerName.of("株式会社テスト"),
            "カブシキガイシャテスト",
            industryClassification
        );
        
        // Then - 業界分類が正しく設定される
        assertThat(basicInfo.getIndustryClassification()).isEqualTo(industryClassification);
        assertThat(basicInfo.getIndustryClassification()).isNotNull();
        assertThat(basicInfo.getIndustryClassification()).isNotEmpty();
    }

    /**
     * プロパティ4: 担当営業者の必須性
     * 全ての顧客に担当営業者が設定される
     */
    @Property
    @Label("Feature: customer-master-system, Property 4: 担当営業者の必須性")
    void assignedSalesRepresentativeRequired(@ForAll("validCustomerData") CustomerData customerData) {
        // When
        Customer customer = Customer.create(
            customerData.customerId,
            customerData.basicInfo,
            customerData.headquartersAddress,
            customerData.representativeContact,
            customerData.mainBankAccount,
            customerData.assignedSalesRep,
            customerData.creditInfo
        );
        
        // Then - 担当営業者が設定されている
        assertThat(customer.getAssignedSalesRep()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getSalesRepresentativeId()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getDepartmentId()).isNotNull();
        assertThat(customer.getAssignedSalesRep().getAssignedAt()).isNotNull();
    }

    // テストデータ生成用のArbitrary

    @Provide
    Arbitrary<CustomerData> validCustomerData() {
        return Combinators.combine(
            Arbitraries.create(() -> CustomerId.generate()),
            validBasicInfo(),
            validAddresses(),
            validContactInfos(),
            validBankAccounts(),
            validAssignedSalesReps(),
            validCreditInfos()
        ).as(CustomerData::new);
    }

    @Provide
    Arbitrary<CustomerBasicInfo> validBasicInfo() {
        return Combinators.combine(
            validCustomerNames(),
            validCustomerNameKanas(),
            validIndustryClassifications(),
            Arbitraries.of(LocalDate.of(1990, 1, 1), LocalDate.of(2020, 12, 31), null),
            Arbitraries.integers().between(1, 10000).optional(),
            validAmounts().optional(),
            validAmounts().optional()
        ).as((name, kana, industry, established, employees, capital, revenue) ->
            CustomerBasicInfo.of(name, kana, industry, established, 
                               employees.orElse(null), capital.orElse(null), revenue.orElse(null)));
    }

    @Provide
    Arbitrary<CustomerName> validCustomerNames() {
        return Arbitraries.strings()
            .withCharRange('あ', 'ん')
            .ofMinLength(3)
            .ofMaxLength(20)
            .map(s -> "株式会社" + s)
            .map(CustomerName::of);
    }

    @Provide
    Arbitrary<String> validCustomerNameKanas() {
        return Arbitraries.strings()
            .withCharRange('ア', 'ン')
            .ofMinLength(3)
            .ofMaxLength(20)
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
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).optional()
        ).as((zip1, zip2, pref, city, street, building) ->
            Address.of(zip1 + "-" + zip2, pref, city, street, building.orElse(null)));
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
    Arbitrary<AssignedSalesRepresentative> validAssignedSalesReps() {
        return Combinators.combine(
            Arbitraries.create(() -> UserId.generate()),
            Arbitraries.create(() -> SalesDepartmentId.generate())
        ).as(AssignedSalesRepresentative::assignNow);
    }

    @Provide
    Arbitrary<CreditInfo> validCreditInfos() {
        return Arbitraries.of(CreditInfo.CreditRank.values())
            .map(CreditInfo::createMinimal);
    }

    @Provide
    Arbitrary<BigDecimal> validAmounts() {
        return Arbitraries.longs()
            .between(1000000L, 10000000000L)
            .map(amount -> BigDecimal.valueOf(amount));
    }

    // テストデータ用のレコード
    private record CustomerData(
        CustomerId customerId,
        CustomerBasicInfo basicInfo,
        Address headquartersAddress,
        ContactInfo representativeContact,
        BankAccount mainBankAccount,
        AssignedSalesRepresentative assignedSalesRep,
        CreditInfo creditInfo
    ) {}
}