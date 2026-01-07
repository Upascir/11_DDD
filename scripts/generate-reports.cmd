@echo off
echo ========================================
echo MDMシステム - レポート生成
echo ========================================

echo.
echo [1/4] テスト実行とカバレッジ測定...
call mvnw.cmd clean test

if %ERRORLEVEL% neq 0 (
    echo エラー: テスト実行に失敗しました
    exit /b 1
)

echo.
echo [2/4] JaCoCoカバレッジレポート生成...
call mvnw.cmd jacoco:report

echo.
echo [3/4] Surefireテストレポート生成...
call mvnw.cmd surefire-report:report

echo.
echo [4/4] Maven統合サイト生成...
call mvnw.cmd site

echo.
echo ========================================
echo レポート生成完了！
echo ========================================
echo.
echo 生成されたレポート:
echo - テスト結果: docs\surefire-report.html
echo - カバレッジ: docs\jacoco\index.html
echo - 統合サイト: docs\index.html
echo.
echo ブラウザでレポートを開く場合:
echo start docs\index.html
echo.
pause