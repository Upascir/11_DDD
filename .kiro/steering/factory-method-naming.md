# ファクトリメソッド命名規約

## 基本パターン

### 1. **`of`** - 既存データからの作成
**用途**: 既存の値やパラメータから値オブジェクトを作成する場合（最も汎用的）
```java
// EntityIdサブクラス
CustomerId.of("existing-id")
UserId.of("user-123")

// Nameサブクラス  
CustomerName.of("株式会社テスト")
UserName.of("田中太郎")

// 複雑な値オブジェクト
Address.of("100-0001", "東京都", "千代田区", "丸の内1-1-1")
ContactInfo.of("03-1234-5678", "03-1234-5679", "test@example.com")
BankAccount.of("0001", "テスト銀行", "001", "テスト支店", AccountType.ORDINARY, "1234567", "テスト")
```

### 2. **`generate`** - 新規ID生成
**用途**: 新しいUUID IDを自動生成する場合（EntityIdサブクラス専用）
```java
CustomerId.generate()
UserId.generate()
AssignmentChangeRequestId.generate()
SalesDepartmentId.generate()
```

### 3. **`create`** - 新規エンティティ/集約の作成
**用途**: 新しいエンティティや集約を作成する場合（ビジネスロジック含む）
```java
Customer.create(customerId, basicInfo, address, contact, bankAccount, salesRep, creditInfo)
User.create(userId, userName, role, departmentId)
SalesDepartment.create(departmentId, departmentName, initialManager)
AssignmentChangeRequest.create(customerId, requesterId, currentAssignment, newSalesRepId, newDepartmentId, reason)
```

### 4. **`restore`** - 永続化からの復元
**用途**: データベースなどの永続化層からエンティティを復元する場合
```java
Customer.restore(customerId, basicInfo, address, branchAddresses, contact, contactPersons, ...)
User.restore(userId, userName, role, departmentId, status, lastLoginAt, ...)
SalesDepartment.restore(departmentId, departmentName, salesReps, managers, status, ...)
AssignmentChangeRequest.restore(requestId, customerId, requesterId, currentAssignment, ...)
```

### 5. **`from`** - 他のオブジェクトからの変換
**用途**: 既存のオブジェクトから別の形式に変換する場合
```java
CustomerSnapshot.from(customer)  // CustomerからCustomerSnapshotに変換
```

### 6. **`approve` / `reject`** - 承認アクション
**用途**: 承認・却下の状態を持つ値オブジェクトを作成する場合
```java
Approval.approve(approverId, comment)
Approval.reject(approverId, reason)
AssignmentApproval.approve(approverId, departmentId, comment)
AssignmentApproval.reject(approverId, departmentId, reason)
```

### 7. **`assignNow`** - 現在時刻での割り当て
**用途**: 現在時刻で何かを割り当てる場合（特定ドメイン用）
```java
AssignedSalesRepresentative.assignNow(salesRepId, departmentId)
```

### 8. **特殊なファクトリメソッド**
```java
// 承認待ち状態の作成
Approval.createPending()

// 期限切れ状態の作成  
Approval.expire()
```

## 選択指針

### オブジェクト種別による選択
- **値オブジェクト**: 基本的に`of`を使用
- **EntityID**: 新規は`generate`、既存は`of`
- **エンティティ/集約**: 新規は`create`、復元は`restore`
- **変換**: `from`を使用
- **ビジネスアクション**: 具体的なアクション名を使用（`approve`, `reject`, `assignNow`など）

### 判断フローチャート
1. **EntityIdサブクラス**？
   - 新規ID生成 → `generate()`
   - 既存ID値から作成 → `of(String)`

2. **エンティティ/集約**？
   - 新規作成（ビジネスロジック含む） → `create(...)`
   - 永続化からの復元 → `restore(...)`

3. **型変換**？
   - 他のオブジェクトから変換 → `from(SourceType)`

4. **ビジネスアクション**？
   - 具体的なアクション名を使用 → `approve(...)`, `reject(...)`, `assignNow(...)`

5. **その他の値オブジェクト**？
   - デフォルト → `of(...)`

## 命名の一貫性

この規約により、メソッド名を見るだけでその目的と使用場面が明確に分かります：

- **`of`**: 「この値から作って」
- **`generate`**: 「新しいIDを生成して」
- **`create`**: 「新しく作って（ビジネスロジック含む）」
- **`restore`**: 「永続化から復元して」
- **`from`**: 「これから変換して」
- **アクション系**: 「このアクションを実行して」

特に`create`と`restore`の使い分けは、新規作成と永続化からの復元という重要な区別を表現しています。