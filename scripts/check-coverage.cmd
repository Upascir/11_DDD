@echo off
echo ========================================
echo 法人マスタ管理システム - カバレッジチェック
echo ========================================

echo.
echo テスト実行とカバレッジ測定中...
call mvnw.cmd clean test jacoco:report

if %ERRORLEVEL% neq 0 (
    echo エラー: テスト実行に失敗しました
    exit /b 1
)

echo.
echo カバレッジ閾値チェック中...
call mvnw.cmd jacoco:check

if %ERRORLEVEL% neq 0 (
    echo.
    echo ========================================
    echo 警告: カバレッジが閾値を下回っています
    echo ========================================
    echo.
    echo 現在の閾値設定:
    echo - 全体ライン: 80%%以上
    echo - 全体ブランチ: 70%%以上  
    echo - ドメイン層: 90%%以上
    echo.
    echo 詳細なカバレッジレポート:
    echo target\site\jacoco\index.html
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ========================================
    echo ✅ カバレッジチェック成功！
    echo ========================================
    echo.
    echo 全ての閾値をクリアしています:
    echo - 全体ライン: 80%%以上 ✅
    echo - 全体ブランチ: 70%%以上 ✅
    echo - ドメイン層: 90%%以上 ✅
    echo.
    echo 詳細レポート: target\site\jacoco\index.html
    echo.
)

pause