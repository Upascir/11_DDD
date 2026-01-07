-- 法人マスタ管理システム用データベース初期化スクリプト

-- データベースが存在しない場合は作成（Docker Composeで既に作成されているため、通常は不要）
-- CREATE DATABASE IF NOT EXISTS customer_master;

-- ユーザーの権限設定（Docker Composeで既に設定されているため、通常は不要）
-- GRANT ALL PRIVILEGES ON DATABASE customer_master TO customer_master_user;

-- 開発用の初期データ（必要に応じて追加）
-- 例: テスト用のマスタデータなど

-- 日本標準産業分類の一部（サンプル）
-- CREATE TABLE IF NOT EXISTS industry_classifications (
--     code VARCHAR(10) PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     parent_code VARCHAR(10),
--     level INTEGER NOT NULL
-- );

-- 銀行マスタのサンプルデータ
-- CREATE TABLE IF NOT EXISTS banks (
--     code VARCHAR(10) PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     kana VARCHAR(255)
-- );

-- INSERT INTO banks (code, name, kana) VALUES
-- ('0001', '日本銀行', 'ニッポンギンコウ'),
-- ('0005', 'みずほ銀行', 'ミズホギンコウ'),
-- ('0009', '三井住友銀行', 'ミツイスミトモギンコウ'),
-- ('0017', '三菱UFJ銀行', 'ミツビシユーエフジェイギンコウ');

-- 開発環境用のログ出力設定
\echo 'Database initialization completed for Customer Master System'