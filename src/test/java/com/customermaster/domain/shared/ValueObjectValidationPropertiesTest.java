package com.customermaster.domain.shared;

import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.customer.CustomerName;
import com.customermaster.domain.salesdepartment.DepartmentName;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.UserName;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 値オブジェクトのバリデーションプロパティテスト
 * 
 * プロパティ1: 値オブジェクトの不変性（バリデーション部分）
 * 検証要件: 2.1-2.8
 */
class ValueObjectValidationPropertiesTest {

    /**
     * プロパティ1: 無効な入力に対する例外発生
     * null、空文字、空白のみの文字列は例外を発生させる
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効入力での例外発生")
    void invalidInputThrowsException(@ForAll("invalidString") String invalidInput) {
        // EntityId系
        assertThatThrownBy(() -> CustomerId.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID値は必須です");
        
        assertThatThrownBy(() -> UserId.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID値は必須です");
        
        assertThatThrownBy(() -> SalesDepartmentId.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID値は必須です");
        
        // Name系
        assertThatThrownBy(() -> CustomerName.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は必須です");
        
        assertThatThrownBy(() -> UserName.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は必須です");
        
        assertThatThrownBy(() -> DepartmentName.of(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は必須です");
    }

    /**
     * プロパティ1: 長すぎる名前に対する例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 長すぎる名前での例外発生")
    void tooLongNameThrowsException(@ForAll("longNameNonEmpty") String longName) {
        // UserName (最大50文字)
        assertThatThrownBy(() -> UserName.of(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は50文字以内で入力してください");
        
        // DepartmentName (最大50文字)
        assertThatThrownBy(() -> DepartmentName.of(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は50文字以内で入力してください");
    }

    /**
     * プロパティ1: CustomerNameの長さ制限（100文字）
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: CustomerName長さ制限")
    void customerNameLengthLimit(@ForAll("longCustomerNameNonEmpty") String longName) {
        assertThatThrownBy(() -> CustomerName.of(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("名前は100文字以内で入力してください");
    }

    /**
     * プロパティ1: 無効な郵便番号形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効郵便番号での例外発生")
    void invalidPostalCodeThrowsException(@ForAll("invalidPostalCode") String invalidPostalCode) {
        assertThatThrownBy(() -> new Address(invalidPostalCode, "東京都", "渋谷区", "1-1-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("郵便番号は「123-4567」の形式で入力してください");
    }

    /**
     * プロパティ1: 無効な電話番号形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効電話番号での例外発生")
    void invalidPhoneNumberThrowsException(@ForAll("invalidPhoneNumber") String invalidPhone) {
        assertThatThrownBy(() -> new ContactInfo(invalidPhone, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("電話番号は「03-1234-5678」の形式で入力してください");
    }

    /**
     * プロパティ1: 無効なメールアドレス形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効メールアドレスでの例外発生")
    void invalidEmailThrowsException(@ForAll("invalidEmail") String invalidEmail) {
        assertThatThrownBy(() -> new ContactInfo("03-1234-5678", null, invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("メールアドレスの形式が正しくありません");
    }

    /**
     * プロパティ1: 無効な銀行コード形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効銀行コードでの例外発生")
    void invalidBankCodeThrowsException(@ForAll("invalidBankCodeNonEmpty") String invalidBankCode) {
        assertThatThrownBy(() -> new BankAccount(invalidBankCode, "テスト銀行", "001", "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("銀行コードは4桁の数字で入力してください");
    }

    /**
     * プロパティ1: 無効な支店コード形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効支店コードでの例外発生")
    void invalidBranchCodeThrowsException(@ForAll("invalidBranchCodeNonEmpty") String invalidBranchCode) {
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", invalidBranchCode, "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("支店コードは3桁の数字で入力してください");
    }

    /**
     * プロパティ1: 無効な口座番号形式での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 無効口座番号での例外発生")
    void invalidAccountNumberThrowsException(@ForAll("invalidAccountNumberNonEmpty") String invalidAccountNumber) {
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", "001", "本店", 
                BankAccount.AccountType.ORDINARY, invalidAccountNumber, "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("口座番号は7桁の数字で入力してください");
    }

    // カスタムジェネレーター
    @Provide
    Arbitrary<String> invalidString() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just(""),
                Arbitraries.just("   "),
                Arbitraries.just("\t"),
                Arbitraries.just("\n"),
                Arbitraries.just("  \t  \n  ")
        );
    }

    @Provide
    Arbitrary<String> invalidPostalCode() {
        return Arbitraries.oneOf(
                Arbitraries.just("1234567"),      // ハイフンなし
                Arbitraries.just("123-456"),      // 桁数不足
                Arbitraries.just("12-34567"),     // 前半桁数不足
                Arbitraries.just("1234-567"),     // 後半桁数不足
                Arbitraries.just("abc-defg"),     // 数字以外
                Arbitraries.just("123-456a"),     // 一部数字以外
                Arbitraries.just("123--4567"),    // ハイフン重複
                Arbitraries.just("-123-4567")     // 先頭ハイフン
        );
    }

    @Provide
    Arbitrary<String> invalidPhoneNumber() {
        return Arbitraries.oneOf(
                Arbitraries.just("1234567890"),   // ハイフンなし
                Arbitraries.just("12-34-56"),     // 桁数不足
                Arbitraries.just("abc-defg-hijk"), // 数字以外
                Arbitraries.just("03-1234-567a"), // 一部数字以外
                Arbitraries.just("03--1234-5678"), // ハイフン重複
                Arbitraries.just("-03-1234-5678"), // 先頭ハイフン
                Arbitraries.just("03-1234-"),     // 末尾ハイフン
                Arbitraries.just("1-2-3"),        // 短すぎる
                Arbitraries.just("12345-67-890")  // 桁数が合わない
        );
    }

    @Provide
    Arbitrary<String> invalidEmail() {
        return Arbitraries.oneOf(
                Arbitraries.just("invalid"),      // @なし
                Arbitraries.just("@example.com"), // ローカル部なし
                Arbitraries.just("user@"),        // ドメイン部なし
                Arbitraries.just("user@.com"),    // ドメイン名なし
                Arbitraries.just("user@example"), // TLDなし
                Arbitraries.just("user@@example.com"), // @重複
                Arbitraries.just("user@example..com"), // ドット重複
                Arbitraries.just("user@example.c"),    // TLD短すぎる
                Arbitraries.just("user@-example.com"), // ドメイン先頭ハイフン
                Arbitraries.just("user@example-.com")  // ドメイン末尾ハイフン
        );
    }

    @Provide
    Arbitrary<String> invalidBankCodeNonEmpty() {
        return Arbitraries.oneOf(
                Arbitraries.just("123"),          // 桁数不足
                Arbitraries.just("12345"),        // 桁数超過
                Arbitraries.just("abc1"),         // 数字以外
                Arbitraries.just("12a4")          // 一部数字以外
        );
    }

    @Provide
    Arbitrary<String> invalidBranchCodeNonEmpty() {
        return Arbitraries.oneOf(
                Arbitraries.just("12"),           // 桁数不足
                Arbitraries.just("1234"),         // 桁数超過
                Arbitraries.just("abc"),          // 数字以外
                Arbitraries.just("1a3")           // 一部数字以外
        );
    }

    @Provide
    Arbitrary<String> invalidAccountNumberNonEmpty() {
        return Arbitraries.oneOf(
                Arbitraries.just("123456"),       // 桁数不足
                Arbitraries.just("12345678"),     // 桁数超過
                Arbitraries.just("abcdefg"),      // 数字以外
                Arbitraries.just("123456a")       // 一部数字以外
        );
    }

    @Provide
    Arbitrary<String> longNameNonEmpty() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(51)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> longCustomerNameNonEmpty() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(101)
                .ofMaxLength(150);
    }
}