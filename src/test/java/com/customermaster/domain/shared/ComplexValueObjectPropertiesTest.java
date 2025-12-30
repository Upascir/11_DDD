package com.customermaster.domain.shared;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 複合値オブジェクトのプロパティベーステスト
 * 
 * プロパティ1: 値オブジェクトの不変性
 * 検証要件: 2.1-2.8
 */
class ComplexValueObjectPropertiesTest {

    /**
     * プロパティ1: Addressの不変性と等価性
     * 同じ値から作成されたAddressは等価であり、異なる値から作成されたものは非等価である
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: Address不変性と等価性")
    void addressImmutabilityAndEquality(@ForAll("validPostalCode") String postalCode1,
                                       @ForAll @NotBlank @StringLength(max = 20) String prefecture1,
                                       @ForAll @NotBlank @StringLength(max = 20) String city1,
                                       @ForAll @NotBlank @StringLength(max = 50) String streetAddress1,
                                       @ForAll("optionalBuilding") String building1,
                                       @ForAll("validPostalCode") String postalCode2,
                                       @ForAll @NotBlank @StringLength(max = 20) String prefecture2,
                                       @ForAll @NotBlank @StringLength(max = 20) String city2,
                                       @ForAll @NotBlank @StringLength(max = 50) String streetAddress2,
                                       @ForAll("optionalBuilding") String building2) {
        
        // 異なる住所を想定
        Assume.that(!postalCode1.equals(postalCode2) || 
                   !prefecture1.trim().equals(prefecture2.trim()) ||
                   !city1.trim().equals(city2.trim()) ||
                   !streetAddress1.trim().equals(streetAddress2.trim()));
        
        Address address1a = Address.of(postalCode1, prefecture1, city1, streetAddress1, building1);
        Address address1b = Address.of(postalCode1, prefecture1, city1, streetAddress1, building1);
        Address address2 = Address.of(postalCode2, prefecture2, city2, streetAddress2, building2);
        
        // 同じ値から作成されたAddressは等価
        assertThat(address1a).isEqualTo(address1b);
        assertThat(address1a.hashCode()).isEqualTo(address1b.hashCode());
        
        // 異なる値から作成されたAddressは非等価
        assertThat(address1a).isNotEqualTo(address2);
        
        // 値は不変（トリムされる）
        assertThat(address1a.getPostalCode()).isEqualTo(postalCode1);
        assertThat(address1a.getPrefecture()).isEqualTo(prefecture1.trim());
        assertThat(address1a.getCity()).isEqualTo(city1.trim());
        assertThat(address1a.getStreetAddress()).isEqualTo(streetAddress1.trim());
        
        // 完全住所文字列が生成される
        String fullAddress = address1a.getFullAddress();
        assertThat(fullAddress).contains(postalCode1);
        assertThat(fullAddress).contains(prefecture1.trim());
        assertThat(fullAddress).contains(city1.trim());
        assertThat(fullAddress).contains(streetAddress1.trim());
    }

    /**
     * プロパティ1: ContactInfoの不変性と等価性
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: ContactInfo不変性と等価性")
    void contactInfoImmutabilityAndEquality(@ForAll("validPhoneNumber") String phone1,
                                           @ForAll("optionalPhoneNumber") String fax1,
                                           @ForAll("optionalEmail") String email1,
                                           @ForAll("validPhoneNumber") String phone2,
                                           @ForAll("optionalPhoneNumber") String fax2,
                                           @ForAll("optionalEmail") String email2) {
        
        // 異なる連絡先を想定
        Assume.that(!phone1.equals(phone2));
        
        ContactInfo contact1a = ContactInfo.of(phone1, fax1, email1);
        ContactInfo contact1b = ContactInfo.of(phone1, fax1, email1);
        ContactInfo contact2 = ContactInfo.of(phone2, fax2, email2);
        
        // 同じ値から作成されたContactInfoは等価
        assertThat(contact1a).isEqualTo(contact1b);
        assertThat(contact1a.hashCode()).isEqualTo(contact1b.hashCode());
        
        // 異なる値から作成されたContactInfoは非等価
        assertThat(contact1a).isNotEqualTo(contact2);
        
        // 値は不変（空文字列はnullに正規化される）
        assertThat(contact1a.getPhoneNumber()).isEqualTo(phone1);
        String expectedFax1 = (fax1 != null && fax1.trim().isEmpty()) ? null : fax1;
        String expectedEmail1 = (email1 != null && email1.trim().isEmpty()) ? null : email1;
        assertThat(contact1a.getFaxNumber()).isEqualTo(expectedFax1);
        assertThat(contact1a.getEmailAddress()).isEqualTo(expectedEmail1);
        
        // オプション項目の判定メソッドが正しく動作
        assertThat(contact1a.hasFaxNumber()).isEqualTo(expectedFax1 != null && !expectedFax1.isEmpty());
        assertThat(contact1a.hasEmailAddress()).isEqualTo(expectedEmail1 != null && !expectedEmail1.isEmpty());
    }

    /**
     * プロパティ1: BankAccountの不変性と等価性
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: BankAccount不変性と等価性")
    void bankAccountImmutabilityAndEquality(@ForAll("validBankCode") String bankCode1,
                                          @ForAll("validBankName") String bankName1,
                                          @ForAll("validBranchCode") String branchCode1,
                                          @ForAll("validBranchName") String branchName1,
                                          @ForAll("accountType") BankAccount.AccountType accountType1,
                                          @ForAll("validAccountNumber") String accountNumber1,
                                          @ForAll("validHolderName") String holderName1,
                                          @ForAll("validBankCode") String bankCode2,
                                          @ForAll("validBankName") String bankName2,
                                          @ForAll("validBranchCode") String branchCode2,
                                          @ForAll("validBranchName") String branchName2,
                                          @ForAll("accountType") BankAccount.AccountType accountType2,
                                          @ForAll("validAccountNumber") String accountNumber2,
                                          @ForAll("validHolderName") String holderName2) {
        
        // 異なる口座を想定（銀行コード、支店コード、口座種別、口座番号のいずれかが異なる）
        Assume.that(!bankCode1.equals(bankCode2) || 
                   !branchCode1.equals(branchCode2) ||
                   !accountType1.equals(accountType2) ||
                   !accountNumber1.equals(accountNumber2));
        
        BankAccount account1a = BankAccount.of(bankCode1, bankName1, branchCode1, branchName1, 
                                               accountType1, accountNumber1, holderName1);
        BankAccount account1b = BankAccount.of(bankCode1, bankName1, branchCode1, branchName1, 
                                               accountType1, accountNumber1, holderName1);
        BankAccount account2 = BankAccount.of(bankCode2, bankName2, branchCode2, branchName2, 
                                              accountType2, accountNumber2, holderName2);
        
        // 同じ値から作成されたBankAccountは等価
        assertThat(account1a).isEqualTo(account1b);
        assertThat(account1a.hashCode()).isEqualTo(account1b.hashCode());
        
        // 異なる値から作成されたBankAccountは非等価
        assertThat(account1a).isNotEqualTo(account2);
        
        // 値は不変
        assertThat(account1a.getBankCode()).isEqualTo(bankCode1);
        assertThat(account1a.getBankName()).isEqualTo(bankName1);
        assertThat(account1a.getBranchCode()).isEqualTo(branchCode1);
        assertThat(account1a.getBranchName()).isEqualTo(branchName1);
        assertThat(account1a.getAccountType()).isEqualTo(accountType1);
        assertThat(account1a.getAccountNumber()).isEqualTo(accountNumber1);
        assertThat(account1a.getAccountHolderName()).isEqualTo(holderName1);
        
        // 表示用文字列が生成される
        String displayString = account1a.getDisplayString();
        assertThat(displayString).contains(bankName1);
        assertThat(displayString).contains(branchName1);
        assertThat(displayString).contains(accountType1.getDisplayName());
        assertThat(displayString).contains(accountNumber1);
        assertThat(displayString).contains(holderName1);
    }

    // カスタムジェネレーター
    @Provide
    Arbitrary<String> validPostalCode() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(3)
                .map(prefix -> prefix + "-")
                .flatMap(prefix -> Arbitraries.strings()
                        .withCharRange('0', '9')
                        .ofLength(4)
                        .map(suffix -> prefix + suffix));
    }

    @Provide
    Arbitrary<String> optionalBuilding() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just(""),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
        );
    }

    @Provide
    Arbitrary<String> validPhoneNumber() {
        return Arbitraries.oneOf(
                Arbitraries.just("03-1234-5678"),
                Arbitraries.just("06-9876-5432"),
                Arbitraries.just("090-1111-2222"),
                Arbitraries.just("0120-123-456"),
                Arbitraries.just("050-1234-567")
        );
    }

    @Provide
    Arbitrary<String> optionalPhoneNumber() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just(""),
                validPhoneNumber()
        );
    }

    @Provide
    Arbitrary<String> optionalEmail() {
        return Arbitraries.oneOf(
                Arbitraries.just((String) null),
                Arbitraries.just(""),
                Arbitraries.just("test@example.com"),
                Arbitraries.just("user@company.co.jp"),
                Arbitraries.just("admin@domain.org")
        );
    }

    @Provide
    Arbitrary<String> validBankCode() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(4);
    }

    @Provide
    Arbitrary<String> validBranchCode() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(3);
    }

    @Provide
    Arbitrary<String> validAccountNumber() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(7);
    }

    @Provide
    Arbitrary<BankAccount.AccountType> accountType() {
        return Arbitraries.of(BankAccount.AccountType.class);
    }

    @Provide
    Arbitrary<String> validBankName() {
        return Arbitraries.oneOf(
                Arbitraries.just("三菱UFJ銀行"),
                Arbitraries.just("みずほ銀行"),
                Arbitraries.just("三井住友銀行"),
                Arbitraries.just("りそな銀行")
        );
    }

    @Provide
    Arbitrary<String> validBranchName() {
        return Arbitraries.oneOf(
                Arbitraries.just("本店"),
                Arbitraries.just("新宿支店"),
                Arbitraries.just("渋谷支店"),
                Arbitraries.just("池袋支店")
        );
    }

    @Provide
    Arbitrary<String> validHolderName() {
        return Arbitraries.oneOf(
                Arbitraries.just("田中太郎"),
                Arbitraries.just("佐藤花子"),
                Arbitraries.just("鈴木一郎"),
                Arbitraries.just("高橋美咲")
        );
    }
}