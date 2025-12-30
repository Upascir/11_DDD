# 技術スタック

## バックエンドスタック
- **Java**: 17 (LTS)
- **フレームワーク**: Spring Boot 3.2.1
- **セキュリティ**: Spring Security (JWT + セッションベース認証)
- **データアクセス**: Spring Data JPA with Hibernate
- **データベース**: PostgreSQL (本番)、H2 (テスト)
- **ビルドツール**: Maven 3.6+
- **テスト**: JUnit 5、Mockito、jqwik (プロパティベーステスト)

## フロントエンドスタック（予定）
- **言語**: TypeScript
- **フレームワーク**: Refine
- **ビルドツール**: Vite
- **UIライブラリ**: Material UI

## 開発ツール
- **コンテナ**: Docker (PostgreSQL)
- **Dev Container**: チーム標準化用に利用可能
- **ホットリロード**: Spring Boot DevTools

## 共通コマンド

### 開発環境セットアップ
```bash
# PostgreSQL起動（Kiro開発推奨）
scripts\start-dev-env.cmd

# 依存関係インストール（初回のみ）
./mvnw clean install

# 開発モードでアプリケーション起動
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 開発環境停止
scripts\stop-dev-env.cmd
```

### テスト
```bash
# 全テスト実行
./mvnw test

# 特定プロファイルでテスト実行
./mvnw test -Dspring.profiles.active=test

# プロパティベーステスト実行（jqwik）
./mvnw test -Dtest="**/*Properties"
```

### ビルド＆パッケージ
```bash
# クリーンビルド
./mvnw clean compile

# アプリケーションパッケージ
./mvnw clean package

# テストスキップしてビルド
./mvnw clean package -DskipTests
```

## 設定プロファイル
- **dev**: PostgreSQLを使用した開発環境
- **test**: H2インメモリデータベースを使用したテスト環境
- **prod**: 本番環境設定

## 主要依存関係
- `spring-boot-starter-web`: REST APIエンドポイント
- `spring-boot-starter-data-jpa`: データベースアクセス
- `spring-boot-starter-security`: 認証/認可
- `spring-boot-starter-validation`: 入力検証
- `postgresql`: 本番データベースドライバー
- `jqwik`: プロパティベーステストフレームワーク