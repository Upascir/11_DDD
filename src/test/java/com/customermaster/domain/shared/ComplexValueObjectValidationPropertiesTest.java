package com.customermaster.domain.shared;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 複合値オブジェクトのバリデーションプロパティテスト
 * 
 * プロパティ1: 複合値オブジェクトのバリデーション機能
 * 検証要件: 2.4, 2.5, 2.6
 */
class ComplexValueObjectValidationPropertiesTest {

    /**
     * プロパティ1: Address - 無効な郵便番号での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: Address無効郵便番号での例外発生")
    void addressInvalidPostalCodeThrowsException(@ForAll("invalidPostalCode") String invalidPostalCode) {
        assertThatThrownBy(() -> new Address(invalidPostalCode, "東京都", "渋谷区", "1-1-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("郵便番号は「123-4567」の形式で入力してください");
    }

    /**
     * プロパティ1: Address - 必須項目null/空文字での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: Address必須項目での例外発生")
    void addressRequiredFieldsThrowException(@ForAll("invalidString") String invalidInput) {
        // 郵便番号が無効
        assertThatThrownBy(() -> new Address(invalidInput, "東京都", "渋谷区", "1-1-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 都道府県が無効
        assertThatThrownBy(() -> new Address("123-4567", invalidInput, "渋谷区", "1-1-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 市区町村が無効
        assertThatThrownBy(() -> new Address("123-4567", "東京都", invalidInput, "1-1-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 番地が無効
        assertThatThrownBy(() -> new Address("123-4567", "東京都", "渋谷区", invalidInput, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * プロパティ1: ContactInfo - 無効な電話番号での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: ContactInfo無効電話番号での例外発生")
    void contactInfoInvalidPhoneThrowsException(@ForAll("invalidPhoneNumber") String invalidPhone) {
        assertThatThrownBy(() -> new ContactInfo(invalidPhone, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("電話番号は「03-1234-5678」の形式で入力してください");
    }

    /**
     * プロパティ1: ContactInfo - 無効なFAX番号での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: ContactInfo無効FAX番号での例外発生")
    void contactInfoInvalidFaxThrowsException(@ForAll("invalidPhoneNumber") String invalidFax) {
        assertThatThrownBy(() -> new ContactInfo("03-1234-5678", invalidFax, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FAX番号は「03-1234-5678」の形式で入力してください");
    }

    /**
     * プロパティ1: ContactInfo - 無効なメールアドレスでの例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: ContactInfo無効メールアドレスでの例外発生")
    void contactInfoInvalidEmailThrowsException(@ForAll("invalidEmail") String invalidEmail) {
        assertThatThrownBy(() -> new ContactInfo("03-1234-5678", null, invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("メールアドレスの形式が正しくありません");
    }

    /**
     * プロパティ1: ContactInfo - 必須電話番号null/空文字での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: ContactInfo必須電話番号での例外発生")
    void contactInfoRequiredPhoneThrowsException(@ForAll("invalidString") String invalidPhone) {
        assertThatThrownBy(() -> new ContactInfo(invalidPhone, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("電話番号は必須です");
    }

    /**
     * プロパティ1: BankAccount - 無効な銀行コードでの例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount無効銀行コードでの例外発生")
    void bankAccountInvalidBankCodeThrowsException(@ForAll("invalidBankCode") String invalidBankCode) {
        assertThatThrownBy(() -> new BankAccount(invalidBankCode, "テスト銀行", "001", "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("銀行コードは4桁の数字で入力してください");
    }

    /**
     * プロパティ1: BankAccount - 無効な支店コードでの例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount無効支店コードでの例外発生")
    void bankAccountInvalidBranchCodeThrowsException(@ForAll("invalidBranchCode") String invalidBranchCode) {
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", invalidBranchCode, "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("支店コードは3桁の数字で入力してください");
    }

    /**
     * プロパティ1: BankAccount - 無効な口座番号での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount無効口座番号での例外発生")
    void bankAccountInvalidAccountNumberThrowsException(@ForAll("invalidAccountNumber") String invalidAccountNumber) {
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", "001", "本店", 
                BankAccount.AccountType.ORDINARY, invalidAccountNumber, "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("口座番号は7桁の数字で入力してください");
    }

    /**
     * プロパティ1: BankAccount - 必須項目null/空文字での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount必須項目での例外発生")
    void bankAccountRequiredFieldsThrowException(@ForAll("invalidString") String invalidInput) {
        // 銀行名が無効
        assertThatThrownBy(() -> new BankAccount("0001", invalidInput, "001", "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("銀行名は必須です");
        
        // 支店名が無効
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", "001", invalidInput, 
                BankAccount.AccountType.ORDINARY, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("支店名は必須です");
        
        // 口座名義人が無効
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", "001", "本店", 
                BankAccount.AccountType.ORDINARY, "1234567", invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("口座名義人は必須です");
    }

    /**
     * プロパティ1: BankAccount - null口座種別での例外発生
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount null口座種別での例外発生")
    void bankAccountNullAccountTypeThrowsException() {
        assertThatThrownBy(() -> new BankAccount("0001", "テスト銀行", "001", "本店", 
                null, "1234567", "テスト太郎"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("口座種別は必須です");
    }

    // カスタムジェネレーター（ValueObjectValidationPropertiesTestから再利用）
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
    Arbitrary<String> invalidBankCode() {
        return Arbitraries.oneOf(
                Arbitraries.just("123"),          // 桁数不足
                Arbitraries.just("12345"),        // 桁数超過
                Arbitraries.just("abc1"),         // 数字以外
                Arbitraries.just("12a4")          // 一部数字以外
        );
    }

    @Provide
    Arbitrary<String> invalidBranchCode() {
        return Arbitraries.oneOf(
                Arbitraries.just("12"),           // 桁数不足
                Arbitraries.just("1234"),         // 桁数超過
                Arbitraries.just("abc"),          // 数字以外
                Arbitraries.just("1a3")           // 一部数字以外
        );
    }

    @Provide
    Arbitrary<String> invalidAccountNumber() {
        return Arbitraries.oneOf(
                Arbitraries.just("123456"),       // 桁数不足
                Arbitraries.just("12345678"),     // 桁数超過
                Arbitraries.just("abcdefg"),      // 数字以外
                Arbitraries.just("123456a")       // 一部数字以外
        );
    }
}