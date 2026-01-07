package com.customermaster.domain.customer;

import com.customermaster.domain.shared.Entity;
import com.customermaster.domain.shared.Address;
import com.customermaster.domain.shared.ContactInfo;
import com.customermaster.domain.shared.BankAccount;
import com.customermaster.domain.assign.AssignmentChangeRequest;
import com.customermaster.domain.assign.AssignedSalesRepresentative;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 法人
 * 
 * 法人情報を管理する集約ルート
 */
public class Customer extends Entity<CustomerId> {
    
    private CustomerBasicInfo basicInfo;
    private Address headquartersAddress;
    private List<Address> branchAddresses;
    private ContactInfo representativeContact;
    private List<ContactInfo> contactPersons;
    private BankAccount mainBankAccount;
    private List<BankAccount> subBankAccounts;
    private AssignedSalesRepresentative assignedSalesRep;
    private CustomerStatus status;
    private CreditInfo creditInfo;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private Customer() {
        super(null);
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ（新規法人用）
     * 
     * @param customerId 法人ID
     * @param basicInfo 基本情報
     * @param headquartersAddress 本社住所
     * @param representativeContact 代表連絡先
     * @param mainBankAccount メイン口座
     * @param assignedSalesRep 担当営業者
     * @param creditInfo 信用情報
     */
    private Customer(CustomerId customerId, CustomerBasicInfo basicInfo, Address headquartersAddress,
                    ContactInfo representativeContact, BankAccount mainBankAccount,
                    AssignedSalesRepresentative assignedSalesRep, CreditInfo creditInfo) {
        super(customerId);
        this.basicInfo = Objects.requireNonNull(basicInfo, "基本情報は必須です");
        this.headquartersAddress = Objects.requireNonNull(headquartersAddress, "本社住所は必須です");
        this.branchAddresses = new ArrayList<>();
        this.representativeContact = Objects.requireNonNull(representativeContact, "代表連絡先は必須です");
        this.contactPersons = new ArrayList<>();
        this.mainBankAccount = Objects.requireNonNull(mainBankAccount, "メイン口座は必須です");
        this.subBankAccounts = new ArrayList<>();
        this.assignedSalesRep = Objects.requireNonNull(assignedSalesRep, "担当営業者は必須です");
        this.status = CustomerStatus.DRAFT;
        this.creditInfo = Objects.requireNonNull(creditInfo, "信用情報は必須です");
    }
    
    /**
     * コンストラクタ（既存法人復元用）
     * 
     * @param customerId 法人ID
     * @param basicInfo 基本情報
     * @param headquartersAddress 本社住所
     * @param branchAddresses 支店住所リスト
     * @param representativeContact 代表連絡先
     * @param contactPersons 担当者連絡先リスト
     * @param mainBankAccount メイン口座
     * @param subBankAccounts サブ口座リスト
     * @param assignedSalesRep 担当営業者
     * @param status ステータス
     * @param creditInfo 信用情報
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    private Customer(CustomerId customerId, CustomerBasicInfo basicInfo, Address headquartersAddress,
                    List<Address> branchAddresses, ContactInfo representativeContact, 
                    List<ContactInfo> contactPersons, BankAccount mainBankAccount,
                    List<BankAccount> subBankAccounts, AssignedSalesRepresentative assignedSalesRep,
                    CustomerStatus status, CreditInfo creditInfo, 
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(customerId, createdAt, updatedAt);
        this.basicInfo = Objects.requireNonNull(basicInfo, "基本情報は必須です");
        this.headquartersAddress = Objects.requireNonNull(headquartersAddress, "本社住所は必須です");
        this.branchAddresses = new ArrayList<>(Objects.requireNonNull(branchAddresses, "支店住所リストは必須です"));
        this.representativeContact = Objects.requireNonNull(representativeContact, "代表連絡先は必須です");
        this.contactPersons = new ArrayList<>(Objects.requireNonNull(contactPersons, "担当者連絡先リストは必須です"));
        this.mainBankAccount = Objects.requireNonNull(mainBankAccount, "メイン口座は必須です");
        this.subBankAccounts = new ArrayList<>(Objects.requireNonNull(subBankAccounts, "サブ口座リストは必須です"));
        this.assignedSalesRep = Objects.requireNonNull(assignedSalesRep, "担当営業者は必須です");
        this.status = Objects.requireNonNull(status, "ステータスは必須です");
        this.creditInfo = Objects.requireNonNull(creditInfo, "信用情報は必須です");
        
        validateInvariants();
    }
    
    /**
     * 新規法人を作成
     * 
     * @param customerId 法人ID
     * @param basicInfo 基本情報
     * @param headquartersAddress 本社住所
     * @param representativeContact 代表連絡先
     * @param mainBankAccount メイン口座
     * @param assignedSalesRep 担当営業者
     * @param creditInfo 信用情報
     * @return Customer
     */
    public static Customer create(CustomerId customerId, CustomerBasicInfo basicInfo, 
                                 Address headquartersAddress, ContactInfo representativeContact,
                                 BankAccount mainBankAccount, AssignedSalesRepresentative assignedSalesRep,
                                 CreditInfo creditInfo) {
        return new Customer(customerId, basicInfo, headquartersAddress, representativeContact,
                          mainBankAccount, assignedSalesRep, creditInfo);
    }
    
    /**
     * 既存法人を復元
     * 
     * @param customerId 法人ID
     * @param basicInfo 基本情報
     * @param headquartersAddress 本社住所
     * @param branchAddresses 支店住所リスト
     * @param representativeContact 代表連絡先
     * @param contactPersons 担当者連絡先リスト
     * @param mainBankAccount メイン口座
     * @param subBankAccounts サブ口座リスト
     * @param assignedSalesRep 担当営業者
     * @param status ステータス
     * @param creditInfo 信用情報
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return Customer
     */
    public static Customer restore(CustomerId customerId, CustomerBasicInfo basicInfo,
                                  Address headquartersAddress, List<Address> branchAddresses,
                                  ContactInfo representativeContact, List<ContactInfo> contactPersons,
                                  BankAccount mainBankAccount, List<BankAccount> subBankAccounts,
                                  AssignedSalesRepresentative assignedSalesRep, CustomerStatus status,
                                  CreditInfo creditInfo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Customer(customerId, basicInfo, headquartersAddress, branchAddresses,
                          representativeContact, contactPersons, mainBankAccount, subBankAccounts,
                          assignedSalesRep, status, creditInfo, createdAt, updatedAt);
    }
    
    // ビジネスメソッド
    
    /**
     * 支店住所を追加
     * 
     * @param branchAddress 支店住所
     */
    public void addBranchAddress(Address branchAddress) {
        Objects.requireNonNull(branchAddress, "支店住所は必須です");
        this.branchAddresses.add(branchAddress);
        markAsUpdated();
    }
    
    /**
     * 支店住所を削除
     * 
     * @param index 削除するインデックス
     */
    public void removeBranchAddress(int index) {
        if (index < 0 || index >= branchAddresses.size()) {
            throw new IllegalArgumentException("無効なインデックスです");
        }
        this.branchAddresses.remove(index);
        markAsUpdated();
    }
    
    /**
     * 担当者連絡先を追加
     * 
     * @param contactPerson 担当者連絡先
     */
    public void addContactPerson(ContactInfo contactPerson) {
        Objects.requireNonNull(contactPerson, "担当者連絡先は必須です");
        this.contactPersons.add(contactPerson);
        markAsUpdated();
    }
    
    /**
     * 担当者連絡先を削除
     * 
     * @param index 削除するインデックス
     */
    public void removeContactPerson(int index) {
        if (index < 0 || index >= contactPersons.size()) {
            throw new IllegalArgumentException("無効なインデックスです");
        }
        this.contactPersons.remove(index);
        markAsUpdated();
    }
    
    /**
     * サブ口座を追加
     * 
     * @param subBankAccount サブ口座
     */
    public void addSubBankAccount(BankAccount subBankAccount) {
        Objects.requireNonNull(subBankAccount, "サブ口座は必須です");
        
        if (subBankAccounts.size() >= 3) {
            throw new IllegalStateException("サブ口座は最大3件まで登録できます");
        }
        
        this.subBankAccounts.add(subBankAccount);
        markAsUpdated();
    }
    
    /**
     * サブ口座を削除
     * 
     * @param index 削除するインデックス
     */
    public void removeSubBankAccount(int index) {
        if (index < 0 || index >= subBankAccounts.size()) {
            throw new IllegalArgumentException("無効なインデックスです");
        }
        this.subBankAccounts.remove(index);
        markAsUpdated();
    }
    
    /**
     * 担当営業者を変更
     * 
     * @param newAssignedSalesRep 新しい担当営業者
     */
    public void assignSalesRepresentative(AssignedSalesRepresentative newAssignedSalesRep) {
        this.assignedSalesRep = Objects.requireNonNull(newAssignedSalesRep, "担当営業者は必須です");
        markAsUpdated();
    }
    
    /**
     * 担当者変更申請を作成
     * 
     * @param requesterId 申請者ID
     * @param newSalesRepId 新しい営業担当者ID
     * @param newDepartmentId 新しい営業部ID
     * @param reason 変更理由
     * @return 担当者変更申請
     */
    public AssignmentChangeRequest createAssignmentChangeRequest(UserId requesterId,
                                                               UserId newSalesRepId,
                                                               SalesDepartmentId newDepartmentId,
                                                               String reason) {
        // 編集可能な状態かチェック
        if (!isEditable()) {
            throw new IllegalStateException("現在の状態では担当者変更申請できません: " + status.getDisplayName());
        }
        
        return AssignmentChangeRequest.create(getId(), requesterId, assignedSalesRep, 
                                            newSalesRepId, newDepartmentId, reason);
    }
    
    /**
     * 担当者変更を直接実行
     * 
     * @param newSalesRepId 新しい営業担当者ID
     * @param newDepartmentId 新しい営業部ID
     */
    public void executeAssignmentChange(UserId newSalesRepId, SalesDepartmentId newDepartmentId) {
        AssignedSalesRepresentative newAssignment = AssignedSalesRepresentative.assignNow(
            newSalesRepId, newDepartmentId);
        assignSalesRepresentative(newAssignment);
    }
    
    /**
     * 担当者変更申請に基づいて担当者を変更
     * 
     * @param changeRequest 変更申請
     */
    public void executeAssignmentChange(AssignmentChangeRequest changeRequest) {
        Objects.requireNonNull(changeRequest, "変更申請は必須です");
        
        // 申請の有効性をチェック
        if (!assignedSalesRep.equals(changeRequest.getCurrentAssignment())) {
            throw new IllegalStateException("申請時の担当者と現在の担当者が一致しません");
        }
        
        assignSalesRepresentative(changeRequest.getNewAssignment());
    }
    
    /**
     * 基本情報を更新
     * 
     * @param newBasicInfo 新しい基本情報
     */
    public void updateBasicInfo(CustomerBasicInfo newBasicInfo) {
        this.basicInfo = Objects.requireNonNull(newBasicInfo, "基本情報は必須です");
        markAsUpdated();
    }
    
    /**
     * 本社住所を更新
     * 
     * @param newHeadquartersAddress 新しい本社住所
     */
    public void updateHeadquartersAddress(Address newHeadquartersAddress) {
        this.headquartersAddress = Objects.requireNonNull(newHeadquartersAddress, "本社住所は必須です");
        markAsUpdated();
    }
    
    /**
     * 代表連絡先を更新
     * 
     * @param newRepresentativeContact 新しい代表連絡先
     */
    public void updateRepresentativeContact(ContactInfo newRepresentativeContact) {
        this.representativeContact = Objects.requireNonNull(newRepresentativeContact, "代表連絡先は必須です");
        markAsUpdated();
    }
    
    /**
     * メイン口座を更新
     * 
     * @param newMainBankAccount 新しいメイン口座
     */
    public void updateMainBankAccount(BankAccount newMainBankAccount) {
        this.mainBankAccount = Objects.requireNonNull(newMainBankAccount, "メイン口座は必須です");
        markAsUpdated();
    }
    
    /**
     * 信用情報を更新
     * 
     * @param newCreditInfo 新しい信用情報
     */
    public void updateCreditInfo(CreditInfo newCreditInfo) {
        this.creditInfo = Objects.requireNonNull(newCreditInfo, "信用情報は必須です");
        markAsUpdated();
    }
    
    /**
     * 承認申請
     */
    public void requestApproval() {
        if (!status.canRequestApproval()) {
            throw new IllegalStateException("現在の状態では承認申請できません: " + status.getDisplayName());
        }
        this.status = CustomerStatus.PENDING_APPROVAL;
        markAsUpdated();
    }
    
    /**
     * 承認
     */
    public void approve() {
        if (!status.canBeApproved()) {
            throw new IllegalStateException("現在の状態では承認できません: " + status.getDisplayName());
        }
        this.status = CustomerStatus.APPROVED;
        markAsUpdated();
    }
    
    /**
     * 却下
     */
    public void reject() {
        if (!status.canBeRejected()) {
            throw new IllegalStateException("現在の状態では却下できません: " + status.getDisplayName());
        }
        this.status = CustomerStatus.REJECTED;
        markAsUpdated();
    }
    
    /**
     * 無効化
     */
    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
        markAsUpdated();
    }
    
    /**
     * 支払い信頼性を判定
     * 
     * @return 支払い信頼性
     */
    public boolean isPaymentReliable() {
        return creditInfo.isPaymentReliable();
    }
    
    /**
     * 指定された営業担当者が担当しているかどうかを判定
     * 
     * @param userId ユーザーID
     * @return 担当している場合true
     */
    public boolean isAssignedTo(UserId userId) {
        return assignedSalesRep.isAssignedTo(userId);
    }
    
    /**
     * 指定された営業部に所属しているかどうかを判定
     * 
     * @param departmentId 営業部ID
     * @return 所属している場合true
     */
    public boolean belongsToDepartment(SalesDepartmentId departmentId) {
        return assignedSalesRep.belongsToDepartment(departmentId);
    }
    
    /**
     * 編集可能かどうかを判定
     * 
     * @return 編集可能な場合true
     */
    public boolean isEditable() {
        return status.isEditable();
    }
    
    /**
     * 承認待ち状態かどうかを判定
     * 
     * @return 承認待ち状態の場合true
     */
    public boolean isPendingApproval() {
        return status.isPendingApproval();
    }
    
    /**
     * 不変条件を検証
     */
    private void validateInvariants() {
        // サブ口座は最大3件まで
        if (subBankAccounts.size() > 3) {
            throw new IllegalStateException("サブ口座は最大3件まで登録できます");
        }
    }
    
    // Getters
    
    public CustomerBasicInfo getBasicInfo() { return basicInfo; }
    public Address getHeadquartersAddress() { return headquartersAddress; }
    public List<Address> getBranchAddresses() { return Collections.unmodifiableList(branchAddresses); }
    public ContactInfo getRepresentativeContact() { return representativeContact; }
    public List<ContactInfo> getContactPersons() { return Collections.unmodifiableList(contactPersons); }
    public BankAccount getMainBankAccount() { return mainBankAccount; }
    public List<BankAccount> getSubBankAccounts() { return Collections.unmodifiableList(subBankAccounts); }
    public AssignedSalesRepresentative getAssignedSalesRep() { return assignedSalesRep; }
    public CustomerStatus getStatus() { return status; }
    public CreditInfo getCreditInfo() { return creditInfo; }
    
    @Override
    public String toString() {
        return "Customer{" +
               "id=" + getId() +
               ", customerName=" + basicInfo.getCustomerName() +
               ", status=" + status +
               ", assignedSalesRep=" + assignedSalesRep.getSalesRepresentativeId() +
               '}';
    }
}