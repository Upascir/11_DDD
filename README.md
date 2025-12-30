# 顧客マスタ管理システム

DDDアーキテクチャに基づく顧客マスタ管理システムです。

## 技術スタック

### バックエンド
- Java 17
- Spring Boot 3.2.1
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven

### フロントエンド
- TypeScript
- Refine
- Vite
- Material UI

### テスト
- JUnit 5
- Mockito
- jqwik (プロパティベーステスト)

## 開発環境セットアップ

### 推奨: ローカル環境 + Docker PostgreSQL

Kiroでの開発に最適化された構成です。

#### 前提条件
- Java 17以上
- Docker Desktop（PostgreSQL用）

#### セットアップ手順
```bash
# 1. PostgreSQL起動
scripts\start-dev-env.cmd

# 2. 依存関係インストール（初回のみ）
./mvnw clean install

# 3. アプリケーション起動
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 開発終了時
scripts\stop-dev-env.cmd
```

### 代替案1: 完全ローカル環境

#### 前提条件
- Java 17以上
- Maven 3.6以上
- PostgreSQL 13以上

#### データベース設定
```sql
CREATE DATABASE customer_master;
CREATE USER customer_master_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE customer_master TO customer_master_user;
```

### 代替案2: Dev Container（将来対応予定）

チーム開発での環境統一用に`.devcontainer`設定を用意済みです。

### バックエンド起動

```bash
# 依存関係のインストール
./mvnw clean install

# 開発環境での起動
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 利用可能なサービス
- **アプリケーション**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **フロントエンド**: http://localhost:3000（後で設定）

## プロジェクト構造

```
src/
├── main/
│   ├── java/com/customermaster/
│   │   ├── domain/           # ドメイン層
│   │   ├── application/      # アプリケーション層
│   │   ├── infrastructure/   # インフラストラクチャ層
│   │   └── presentation/     # プレゼンテーション層
│   └── resources/
└── test/
    ├── java/
    └── resources/
```

## 実装フェーズ

実装は以下のフェーズに分けて進行します：

1. **Phase 1**: プロジェクト基盤とドメイン層
2. **Phase 2**: 承認ワークフロー
3. **Phase 3**: インフラストラクチャ層
4. **Phase 4**: アプリケーション層
5. **Phase 5**: プレゼンテーション層

詳細は `.kiro/specs/customer-master-system/tasks.md` を参照してください。