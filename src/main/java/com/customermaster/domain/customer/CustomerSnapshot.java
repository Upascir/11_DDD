package com.customermaster.domain.customer;

import com.customermaster.domain.shared.ValueObject;
import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import com.customermaster.domain.assign.AssignedSalesRepresentative;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 顧客スナップショット
 * 
 * 変更申請時点での顧客情報を保存する不変オブジェクト
 * 差分計算や変更履歴の管理に使用される
 */
public class CustomerSnapshot extends ValueObject {
    
    private final CustomerId customerId;
    private final CustomerBasicInfo basicInfo;
    private final Address headquartersAddress;
    private final List<Address> branchAddresses;
    private final ContactInfo representativeContact;
    private final List<ContactInfo> contactPersons;
    private final BankAccount mainBankAccount;
    private final List<BankAccount> subBankAccounts;
    private final AssignedSalesRepresentative assignedSalesRep;
    private final CreditInfo creditInfo;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private CustomerSnapshot() {
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ
     * 
     * @param customerId 顧客ID
     * @param basicInfo 基本情報
     * @param headquartersAddress 本社住所
     * @param branchAddresses 支店住所リスト
     * @param representativeContact 代表連絡先
     * @param contactPersons 担当者連絡先リスト
     * @param mainBankAccount メイン口座
     * @param subBankAccounts サブ口座リスト
     * @param assignedSalesRep 担当営業者
     * @param creditInfo 信用情報
     */
    private CustomerSnapshot(CustomerId customerId, CustomerBasicInfo basicInfo,
                           Address headquartersAddress, List<Address> branchAddresses,
                           ContactInfo representativeContact, List<ContactInfo> contactPersons,
                           BankAccount mainBankAccount, List<BankAccount> subBankAccounts,
                           AssignedSalesRepresentative assignedSalesRep, CreditInfo creditInfo) {
        this.customerId = Objects.requireNonNull(customerId, "顧客IDは必須です");
        this.basicInfo = Objects.requireNonNull(basicInfo, "基本情報は必須です");
        this.headquartersAddress = Objects.requireNonNull(headquartersAddress, "本社住所は必須です");
        this.branchAddresses = List.copyOf(Objects.requireNonNull(branchAddresses, "支店住所リストは必須です"));
        this.representativeContact = Objects.requireNonNull(representativeContact, "代表連絡先は必須です");
        this.contactPersons = List.copyOf(Objects.requireNonNull(contactPersons, "担当者連絡先リストは必須です"));
        this.mainBankAccount = Objects.requireNonNull(mainBankAccount, "メイン口座は必須です");
        this.subBankAccounts = List.copyOf(Objects.requireNonNull(subBankAccounts, "サブ口座リストは必須です"));
        this.assignedSalesRep = Objects.requireNonNull(assignedSalesRep, "担当営業者は必須です");
        this.creditInfo = Objects.requireNonNull(creditInfo, "信用情報は必須です");
    }
    
    /**
     * 顧客からスナップショットを作成
     * 
     * @param customer 顧客
     * @return CustomerSnapshot
     */
    public static CustomerSnapshot from(Customer customer) {
        return new CustomerSnapshot(
            customer.getId(),  // getCustomerId() -> getId()に修正
            customer.getBasicInfo(),
            customer.getHeadquartersAddress(),
            customer.getBranchAddresses(),
            customer.getRepresentativeContact(),
            customer.getContactPersons(),
            customer.getMainBankAccount(),
            customer.getSubBankAccounts(),
            customer.getAssignedSalesRep(),
            customer.getCreditInfo()
        );
    }
    
    /**
     * テスト用の簡単なスナップショットを作成
     * 
     * @param customerId 顧客ID
     * @param customerName 顧客名
     * @param address 住所
     * @param contactInfo 連絡先
     * @return CustomerSnapshot
     */
    public static CustomerSnapshot of(CustomerId customerId, CustomerName customerName, 
                                    Address address, ContactInfo contactInfo) {
        CustomerBasicInfo basicInfo = CustomerBasicInfo.of(
            customerName, 
            "テストカナ", 
            "製造業"
        );
        
        BankAccount defaultBank = BankAccount.of(
            "0001", "テスト銀行", "001", "テスト支店", 
            BankAccount.AccountType.ORDINARY, "1234567", "テスト"
        );
        AssignedSalesRepresentative defaultSalesRep = AssignedSalesRepresentative.of(
            UserId.generate(), 
            SalesDepartmentId.generate(),
            LocalDateTime.now()
        );
        CreditInfo defaultCredit = CreditInfo.createMinimal(CreditInfo.CreditRank.A);
        
        return new CustomerSnapshot(
            customerId,
            basicInfo,
            address,
            List.of(),  // 支店住所なし
            contactInfo,
            List.of(),  // 担当者連絡先なし
            defaultBank,
            List.of(),  // サブ口座なし
            defaultSalesRep,
            defaultCredit
        );
    }
    
    // Getters
    public CustomerId getCustomerId() { return customerId; }
    public CustomerBasicInfo getBasicInfo() { return basicInfo; }
    public Address getHeadquartersAddress() { return headquartersAddress; }
    public List<Address> getBranchAddresses() { return branchAddresses; }
    public ContactInfo getRepresentativeContact() { return representativeContact; }
    public List<ContactInfo> getContactPersons() { return contactPersons; }
    public BankAccount getMainBankAccount() { return mainBankAccount; }
    public List<BankAccount> getSubBankAccounts() { return subBankAccounts; }
    public AssignedSalesRepresentative getAssignedSalesRep() { return assignedSalesRep; }
    public CreditInfo getCreditInfo() { return creditInfo; }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CustomerSnapshot that = (CustomerSnapshot) other;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(basicInfo, that.basicInfo) &&
               Objects.equals(headquartersAddress, that.headquartersAddress) &&
               Objects.equals(branchAddresses, that.branchAddresses) &&
               Objects.equals(representativeContact, that.representativeContact) &&
               Objects.equals(contactPersons, that.contactPersons) &&
               Objects.equals(mainBankAccount, that.mainBankAccount) &&
               Objects.equals(subBankAccounts, that.subBankAccounts) &&
               Objects.equals(assignedSalesRep, that.assignedSalesRep) &&
               Objects.equals(creditInfo, that.creditInfo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, basicInfo, headquartersAddress, branchAddresses,
                          representativeContact, contactPersons, mainBankAccount, subBankAccounts,
                          assignedSalesRep, creditInfo);
    }
    
    @Override
    public String toString() {
        return "CustomerSnapshot{" +
               "customerId=" + customerId +
               ", basicInfo=" + basicInfo +
               ", assignedSalesRep=" + assignedSalesRep +
               '}';
    }
}