# MDMシステム - 設計ドキュメント

## 概要

本ドキュメントは、MDMシステムのDDD（ドメイン駆動設計）に基づく設計仕様を定義する。複数営業部制による組織管理、担当制法人管理、部署別承認ワークフローを中心とした設計を行う。

## アーキテクチャ

### レイヤーアーキテクチャ

```
┌─────────────────────────────────────┐
│        プレゼンテーション層          │
│     (Web UI, REST API)             │
├─────────────────────────────────────┤
│        アプリケーション層            │
│    (ユースケース, アプリケーション   │
│     サービス, DTOマッピング)        │
├─────────────────────────────────────┤
│           ドメイン層                │
│   (エンティティ, 値オブジェクト,     │
│    集約, ドメインサービス)          │
├─────────────────────────────────────┤
│       インフラストラクチャ層         │
│  (データベース, 外部API, メッセージ) │
└─────────────────────────────────────┘
```

### 技術スタック

- **バックエンド**: Java + Spring Boot
- **データベース**: PostgreSQL
- **外部連携**: REST API (TSR, SAP)
- **フロントエンド**: Refine + Vite + Material UI + TypeScript
- **パッケージ管理**: npm
- **認証**: Spring Security

## 境界づけられたコンテキスト

### 1. 組織管理コンテキスト (Organization Management)

**責務**: 営業部、ユーザー、権限の管理

**主要概念**:
- 営業部 (SalesDepartment)
- ユーザー (User)
- 役割 (Role)

**集約**:
- SalesDepartment集約
- User集約

### 2. 法人管理コンテキスト (Customer Management)

**責務**: 法人情報の管理と担当者の紐づけ

**主要概念**:
- 法人 (Customer)
- 担当営業者 (AssignedSalesRepresentative)
- 法人情報 (CustomerInformation)

**集約**:
- Customer集約

### 3. 承認ワークフローコンテキスト (Approval Workflow)

**責務**: 変更申請と承認プロセスの管理

**主要概念**:
- 法人情報変更申請 (CustomerChangeRequest)
- 担当者変更申請 (AssignmentChangeRequest)
- 営業部間担当者変更申請 (CrossDepartmentAssignmentRequest)
- 承認 (Approval)
- 差分 (Diff)

**集約**:
- CustomerChangeRequest集約
- AssignmentChangeRequest集約

### 4. 信用情報コンテキスト (Credit Information)

**責務**: 外部信用情報の取得と管理

**主要概念**:
- 信用情報 (CreditInformation)
- TSR連携 (TsrIntegration)

**集約**:
- CreditInformation集約

### 5. システム連携コンテキスト (System Integration)

**責務**: 外部システムとの連携管理

**主要概念**:
- SAP連携 (SapIntegration)
- 連携履歴 (IntegrationHistory)

**集約**:
- SapIntegration集約

### 6. システムマスタ管理コンテキスト (System Master)

**責務**: システムで使用するマスタデータの管理

**主要概念**:
- 銀行マスタ (BankMaster)
- 業界分類マスタ (IndustryMaster)

**集約**:
- BankMaster集約
- IndustryMaster集約

## コンテキストマップ

```
┌─────────────────┐    ┌─────────────────┐
│   組織管理      │────│   法人管理      │
│  コンテキスト   │    │  コンテキスト   │
└─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│ 承認ワークフロー │    │   信用情報      │
│  コンテキスト   │    │  コンテキスト   │
└─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│  システム連携   │    │ システムマスタ  │
│  コンテキスト   │    │ 管理コンテキスト │
└─────────────────┘    └─────────────────┘
```

## 集約設計

### 1. Customer集約 (法人管理コンテキスト)

**集約ルート**: Customer

**エンティティ**:
- Customer (法人)

**値オブジェクト**:
- CustomerId (法人ID)
- CustomerBasicInfo (基本情報)
- Address (住所)
- ContactInfo (連絡先情報)
- BankAccount (口座情報)
- AssignedSalesRepresentative (担当営業者)

**不変条件**:
- 法人には必ず担当営業者が設定されている
- 法人名（正式名称）は一意である
- 本社住所は必須である
- メイン口座は必須である

**ライフサイクル**:
```
下書き → 承認待ち → 承認済み
         ↓
        却下 → 承認待ち（再申請時）
```

### 2. CustomerChangeRequest集約 (承認ワークフローコンテキスト)

**集約ルート**: CustomerChangeRequest

**エンティティ**:
- CustomerChangeRequest (法人情報変更申請)

**値オブジェクト**:
- ChangeRequestId (変更申請ID)
- CustomerId (法人ID)
- CustomerSnapshot (変更前スナップショット)
- ProposedCustomerData (変更後データ)
- Approval (承認情報)
- CustomerDataDiff (差分情報)

**不変条件**:
- 承認は同じ営業部の部長のみが実行可能
- 申請者本人が部長でも自己承認は不可
- 承認期限は申請から1週間以内
- 承認待ち状態の申請のみ承認・却下可能

**ライフサイクル**:
```
申請中 → 承認済み
       ↓
      却下 → 申請中（再申請時）
```

### 3. AssignmentChangeRequest集約 (承認ワークフローコンテキスト)

**集約ルート**: AssignmentChangeRequest

**エンティティ**:
- AssignmentChangeRequest (担当者変更申請)

**値オブジェクト**:
- AssignmentChangeRequestId (担当者変更申請ID)
- CustomerId (法人ID)
- CurrentAssignment (現在の担当者)
- ProposedAssignment (変更後の担当者)
- ApprovalRequirements (承認要件)

**不変条件**:
- 同一営業部内の変更は該当部長の承認が必要
- 営業部間の変更は両方の部長の承認が必要
- 申請者本人が部長でも自己承認は不可
- 承認期限は申請から1週間以内

**ライフサイクル**:
```
申請中 → 承認済み（単一承認）
       ↓
      却下

申請中 → 部分承認 → 承認済み（営業部間変更）
       ↓         ↓
      却下      却下
```

### 3. SalesDepartment集約 (組織管理コンテキスト)

**集約ルート**: SalesDepartment

**エンティティ**:
- SalesDepartment (営業部)

**値オブジェクト**:
- SalesDepartmentId (営業部ID)
- DepartmentName (部署名)
- SalesRepresentatives (営業担当者リスト)
- DepartmentManagers (部長リスト)

**不変条件**:
- 営業部には最低1人の部長が設定されている
- 部長は営業担当者の中から選出される

### 4. User集約 (組織管理コンテキスト)

**集約ルート**: User

**エンティティ**:
- User (ユーザー)

**値オブジェクト**:
- UserId (ユーザーID)
- UserName (ユーザー名)
- Role (役割)
- SalesDepartmentId (所属営業部ID)

**不変条件**:
- 営業担当者と部長は営業部に所属している
- システム管理者は情報システム部に所属している

## ドメインサービス

### 1. CustomerAssignmentService (法人割り当てサービス)

**責務**: 法人の担当営業者割り当てロジック

**主要メソッド**:
- `assignSalesRepresentative(CustomerId, SalesRepresentativeId)`
- `validateAssignment(CustomerId, SalesRepresentativeId)`

### 2. ApprovalAuthorizationService (承認権限サービス)

**責務**: 承認権限の検証ロジック

**主要メソッド**:
- `canApproveCustomerChange(CustomerChangeRequestId, DepartmentManagerId)`
- `canApproveAssignmentChange(AssignmentChangeRequestId, DepartmentManagerId)`
- `findEligibleApprovers(ChangeRequestId)`
- `validateSelfApprovalRestriction(RequesterId, ApproverId)`

### 3. CustomerDataDiffService (法人データ差分サービス)

**責務**: 法人データの差分計算ロジック

**主要メソッド**:
- `calculateDiff(CustomerSnapshot, ProposedCustomerData)`
- `generateDiffReport(CustomerDataDiff)`

### 4. CreditInfoSyncService (信用情報同期サービス)

**責務**: TSRからの信用情報取得・更新ロジック

**主要メソッド**:
- `syncCreditInfo(CustomerId)`
- `scheduleAutoUpdate()`

### 5. SapIntegrationService (SAP連携サービス)

**責務**: SAP連携ロジック

**主要メソッド**:
- `syncToSap(CustomerId)`
- `handleSyncError(CustomerId, ErrorInfo)`

## データモデル

### Customer集約のデータ構造

```java
public class Customer {
    private CustomerId customerId;
    private CustomerBasicInfo basicInfo;
    private Address headquartersAddress;
    private List<Address> branchAddresses;
    private ContactInfo representativeContact;
    private List<ContactInfo> contactPersons;
    private BankAccount mainBankAccount;
    private List<BankAccount> subBankAccounts;
    private AssignedSalesRepresentative assignedSalesRep;
    private CustomerStatus status;
    private CreditInformation creditInfo;
    
    // ビジネスメソッド
    public ChangeRequest requestUpdate(ProposedCustomerData proposedData, SalesRepresentative requestedBy);
    public void updateDirectly(ProposedCustomerData proposedData); // 承認なし版
    public boolean isPaymentReliable();
    public void assignSalesRepresentative(SalesRepresentative salesRep);
}
```

### CustomerChangeRequest集約のデータ構造

```java
public class CustomerChangeRequest {
    private ChangeRequestId requestId;
    private CustomerId customerId;
    private CustomerSnapshot originalData;
    private ProposedCustomerData proposedData;
    private SalesRepresentative requestedBy;
    private ApprovalStatus status;
    private Approval approval;
    private LocalDateTime requestedAt;
    private LocalDateTime deadline;
    
    // ビジネスメソッド
    public void approve(DepartmentManager manager, String comment);
    public void reject(DepartmentManager manager, String reason);
    public CustomerDataDiff getDiff();
    public boolean isExpired();
    public void updateProposedData(ProposedCustomerData newData);
}
```

### AssignmentChangeRequest集約のデータ構造

```java
public class AssignmentChangeRequest {
    private AssignmentChangeRequestId requestId;
    private CustomerId customerId;
    private SalesRepresentativeId currentAssignee;
    private SalesRepresentativeId proposedAssignee;
    private SalesDepartmentId currentDepartment;
    private SalesDepartmentId proposedDepartment;
    private SalesRepresentative requestedBy;
    private ApprovalStatus status;
    private List<Approval> approvals; // 営業部間変更の場合は複数
    private LocalDateTime requestedAt;
    private LocalDateTime deadline;
    
    // ビジネスメソッド
    public void approveByDepartmentManager(DepartmentManager manager, String comment);
    public void reject(DepartmentManager manager, String reason);
    public boolean isCrossDepartmentChange();
    public boolean isFullyApproved();
    public List<DepartmentManager> getRequiredApprovers();
}
```

### User集約のデータ構造（更新）

```java
public class User {
    private UserId userId;
    private UserName userName;
    private Role role;
    private SalesDepartmentId departmentId;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    
    // ビジネスメソッド
    public boolean canApproveCustomerChange(CustomerId customerId);
    public boolean canApproveAssignmentChange(AssignmentChangeRequestId requestId);
    public boolean canDirectlyUpdateCustomer(CustomerId customerId);
    public void recordLoginFailure();
    public void recordSuccessfulLogin();
    public boolean isAccountLocked();
}
```

## アプリケーション層設計

### ユースケース

#### 1. 法人管理ユースケース

- `RegisterCustomerUseCase`: 新規法人登録
- `UpdateCustomerUseCase`: 法人情報更新
- `FindCustomerUseCase`: 法人検索・取得
- `ListCustomersUseCase`: 法人一覧取得

#### 2. 承認ワークフローユースケース

- `RequestCustomerUpdateUseCase`: 法人更新申請
- `RequestAssignmentChangeUseCase`: 担当者変更申請
- `ApproveCustomerChangeUseCase`: 法人変更申請承認
- `ApproveAssignmentChangeUseCase`: 担当者変更申請承認
- `RejectChangeRequestUseCase`: 変更申請却下
- `ListPendingRequestsUseCase`: 承認待ち申請一覧
- `ForceApproveRequestUseCase`: システム管理者による強制承認

#### 3. 組織管理ユースケース

- `CreateSalesDepartmentUseCase`: 営業部作成
- `RegisterSalesRepresentativeUseCase`: 営業担当者登録
- `AssignDepartmentManagerUseCase`: 部長設定

### DTOとマッピング

#### CustomerDto

```java
public class CustomerDto {
    private String customerId;
    private String customerName;
    private String customerNameKana;
    private String industryClassification;
    private LocalDate establishedDate;
    private Integer employeeCount;
    private BigDecimal capital;
    private BigDecimal annualRevenue;
    private AddressDto headquartersAddress;
    private List<AddressDto> branchAddresses;
    private ContactInfoDto representativeContact;
    private List<ContactInfoDto> contactPersons;
    private BankAccountDto mainBankAccount;
    private List<BankAccountDto> subBankAccounts;
    private String assignedSalesRepId;
    private String assignedSalesRepName;
    private String status;
    private CreditInfoDto creditInfo;
}
```

## インフラストラクチャ層設計

### リポジトリ実装

#### CustomerRepository

```java
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    
    @Override
    public Optional<Customer> findById(CustomerId customerId);
    
    @Override
    public List<Customer> findByAssignedSalesDepartment(SalesDepartmentId departmentId);
    
    @Override
    public List<Customer> findAll(); // 情報システム部用
    
    @Override
    public void save(Customer customer);
    
    @Override
    public boolean existsByCustomerName(String customerName);
}
```

### 外部システム連携

#### TSR API連携

```java
@Component
public class TsrApiClient {
    
    public TsrCreditData fetchCreditData(String companyCode);
    
    public boolean isApiAvailable();
}
```

#### SAP連携

```java
@Component
public class SapApiClient {
    
    public void upsertCustomer(SapCustomerData customerData);
    
    public SapSyncStatus getSyncStatus(String customerId);
}
```

## セキュリティ設計

### 認証・認可

#### 認証方式
- **認証方法**: ユーザーID・パスワード認証
- **パスワードポリシー**: 8文字以上、英数字混在必須
- **アカウントロック**: 3回失敗で15分間ロック
- **セッション管理**: アクティビティベース（1時間無操作で無効）
- **同時ログイン**: 制限なし（複数端末可能）

#### 役割ベースアクセス制御

```java
@PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
public class CustomerController {
    
    @PreAuthorize("@customerSecurityService.canAccessCustomer(#customerId, authentication)")
    public CustomerDto getCustomer(@PathVariable String customerId);
    
    @PreAuthorize("@customerSecurityService.canUpdateCustomer(#customerId, authentication)")
    public void updateCustomer(@PathVariable String customerId, @RequestBody UpdateCustomerRequest request);
}
```

#### アクセス制御ルール

**営業担当者**:
- 同じ営業部の全法人情報にアクセス可能
- 同じ営業部の全法人情報を更新（変更申請）可能
- 担当法人の担当者変更を申請可能

**部長**:
- 同じ営業部の法人と承認申請にアクセス可能
- 同じ営業部内の担当者変更を直接実行可能
- 営業部間担当者変更の承認権限

**システム管理者**:
- 全データにアクセス可能
- 緊急時の直接更新権限
- 承認申請の強制処理権限
- 営業部間担当者変更の直接実行権限

## 通知システム設計

### 通知イベント

```java
public abstract class DomainEvent {
    private final LocalDateTime occurredAt;
    private final String eventId;
}

public class ChangeRequestSubmitted extends DomainEvent {
    private final ChangeRequestId requestId;
    private final SalesDepartmentId departmentId;
}

public class ChangeRequestApproved extends DomainEvent {
    private final ChangeRequestId requestId;
    private final SalesRepresentativeId requesterId;
}
```

### 通知配信

```java
@EventListener
public class NotificationEventHandler {
    
    @Async
    public void handleChangeRequestSubmitted(ChangeRequestSubmitted event);
    
    @Async
    public void handleChangeRequestApproved(ChangeRequestApproved event);
}
```

## パフォーマンス考慮事項

### データベース設計

- **インデックス**: 法人名、営業部ID、担当営業者IDにインデックス設定
- **パーティショニング**: 履歴テーブルの日付ベースパーティショニング
- **キャッシュ**: 銀行マスタ、業界分類マスタのRedisキャッシュ

### API設計

- **ページネーション**: 法人一覧APIでのページング実装
- **フィルタリング**: 営業部、ステータス、担当者による絞り込み
- **レスポンス最適化**: 必要な項目のみを返すフィールド選択機能

## エラーハンドリング

### ドメイン例外

```java
public class CustomerDomainException extends RuntimeException {
    private final ErrorCode errorCode;
}

public enum ErrorCode {
    CUSTOMER_NOT_FOUND,
    DUPLICATE_CUSTOMER_NAME,
    INVALID_APPROVAL_AUTHORITY,
    APPROVAL_DEADLINE_EXPIRED,
    INVALID_BANK_ACCOUNT
}
```

### 外部システム例外

```java
public class TsrApiException extends RuntimeException {
    private final String apiErrorCode;
    private final String apiErrorMessage;
}

public class SapIntegrationException extends RuntimeException {
    private final String sapErrorCode;
    private final String sapErrorMessage;
}
```

## 監査・ログ設計

### 監査ログ

- **操作ログ**: 全CRUD操作の記録
- **承認ログ**: 承認・却下の詳細記録
- **アクセスログ**: API呼び出しとユーザーアクセスの記録

### ログ項目

```java
public class AuditLog {
    private String logId;
    private LocalDateTime timestamp;
    private String userId;
    private String operation;
    private String targetEntity;
    private String targetId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
}
```

## 今後の拡張性

### 想定される拡張

1. **多言語対応**: 国際展開時の多言語UI
2. **モバイルアプリ**: 営業担当者向けモバイルアプリ
3. **AI機能**: 信用度予測、異常検知
4. **ワークフロー拡張**: 複数段階承認、条件分岐
5. **レポート機能**: BI連携、ダッシュボード

### アーキテクチャ拡張

- **マイクロサービス化**: コンテキスト単位でのサービス分割
- **イベント駆動アーキテクチャ**: 非同期処理の拡張
- **CQRS**: 読み取り専用モデルの分離