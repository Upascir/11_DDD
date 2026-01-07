# プロジェクト構造

## アーキテクチャパターン
このプロジェクトは**ドメイン駆動設計（DDD）**とクリーンアーキテクチャの原則に従い、レイヤー構造で組織化されています。

## ディレクトリ構造

```
src/
├── main/
│   ├── java/com/customermaster/
│   │   ├── domain/              # ドメイン層（コアビジネスロジック）
│   │   │   ├── shared/          # 共有ドメイン概念
│   │   │   ├── customer/        # 法人集約
│   │   │   ├── salesdepartment/ # 営業部門集約
│   │   │   ├── user/            # ユーザー集約
│   │   │   └── assign/          # 担当者割り当て管理
│   │   ├── application/         # アプリケーション層（ユースケース）
│   │   ├── infrastructure/      # インフラストラクチャ層（外部関心事）
│   │   └── presentation/        # プレゼンテーション層（コントローラー、DTO）
│   └── resources/
│       ├── application.yml      # メイン設定
│       ├── application-dev.yml  # 開発プロファイル
│       └── application-test.yml # テストプロファイル
└── test/
    ├── java/                    # テストクラスはmain構造をミラー
    └── resources/
```

## ドメイン層の規約

### 値オブジェクト
- `ValueObject`基底クラスを継承
- 不変設計
- `equals()`、`hashCode()`、`toString()`を実装
- ドメインパッケージに配置（例：`domain.customer.CustomerName`）

### エンティティID
- `EntityId`基底クラスを継承
- UUID ベースの文字列値
- ファクトリメソッド：新規IDは`generate()`、既存IDは`of(String)`
- 例：`CustomerId.generate()`または`CustomerId.of("existing-id")`

### 命名規約
- **集約**: ビジネス概念を表す名詞（Customer、SalesDepartment）
- **値オブジェクト**: 説明的な名前（CustomerName、Address、ContactInfo）
- **エンティティID**: `{Entity}Id`パターン（CustomerId、UserId）
- **サービス**: ドメインサービスは`{Domain}Service`パターン

## パッケージ構成

### ドメインパッケージ
- `domain.shared`: 横断的ドメイン概念（ValueObject、EntityId）
- `domain.customer`: 法人集約と関連値オブジェクト
- `domain.salesdepartment`: 営業部門集約
- `domain.user`: ユーザー集約と認証概念
- `domain.assign`: 担当者割り当て管理（担当者変更申請、認可サービス）

### レイヤー依存関係
- **ドメイン**: 他レイヤーに依存しない（純粋なビジネスロジック）
- **アプリケーション**: ドメインのみに依存
- **インフラストラクチャ**: ドメインとアプリケーションに依存
- **プレゼンテーション**: アプリケーションに依存（ドメインに直接依存しない）

## 設定構造
- **ベース設定**: `application.yml`（共通設定）
- **プロファイル設定**: `application-{profile}.yml`（環境固有）
- **セキュリティ**: `config.SecurityConfig`に集約
- **データベース**: アプリケーションプロパティでJPA設定

## テスト構造
- **単体テスト**: メインパッケージ構造をミラー
- **統合テスト**: テストプロファイルで`@SpringBootTest`を使用
- **プロパティテスト**: ドメイン値オブジェクトテストにjqwikを使用
- **テストプロファイル**: H2データベース用に`@ActiveProfiles("test")`を使用

## ファイル命名パターン
- **エンティティ**: `{ビジネス概念}.java`（Customer.java）
- **値オブジェクト**: `{概念}.java`（CustomerName.java、Address.java）
- **ID**: `{Entity}Id.java`（CustomerId.java）
- **サービス**: `{Domain}Service.java`
- **コントローラー**: `{Resource}Controller.java`
- **DTO**: `{目的}Dto.java`または`{Resource}Request/Response.java`