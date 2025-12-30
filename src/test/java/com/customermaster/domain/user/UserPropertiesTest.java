package com.customermaster.domain.user;

import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.customer.CustomerId;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * User集約のプロパティベーステスト
 * 
 * プロパティ2: ユーザー役割の整合性
 * 検証要件: 1.2
 */
class UserPropertiesTest {

    /**
     * プロパティ2: 営業担当者と部長は営業部への所属が必須
     * 営業担当者（SALES_REPRESENTATIVE）と部長（DEPARTMENT_MANAGER）は
     * 必ず営業部IDが設定されている必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: 営業担当者・部長の営業部所属必須")
    void salesRolesRequireDepartmentId(@ForAll("validUserNames") UserName userName) {
        UserId userId = UserId.generate();
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        
        // 営業担当者は営業部IDが必須
        User salesRep = User.create(userId, userName, Role.SALES_REPRESENTATIVE, departmentId);
        assertThat(salesRep.getRole()).isEqualTo(Role.SALES_REPRESENTATIVE);
        assertThat(salesRep.getDepartmentId()).isEqualTo(departmentId);
        
        // 部長も営業部IDが必須
        User manager = User.create(UserId.generate(), userName, Role.DEPARTMENT_MANAGER, departmentId);
        assertThat(manager.getRole()).isEqualTo(Role.DEPARTMENT_MANAGER);
        assertThat(manager.getDepartmentId()).isEqualTo(departmentId);
        
        // 営業担当者・部長で営業部IDがnullの場合は例外
        assertThatThrownBy(() -> 
            User.create(UserId.generate(), userName, Role.SALES_REPRESENTATIVE, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("営業担当者・部長は営業部への所属が必須です");
            
        assertThatThrownBy(() -> 
            User.create(UserId.generate(), userName, Role.DEPARTMENT_MANAGER, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("営業担当者・部長は営業部への所属が必須です");
    }

    /**
     * プロパティ2: システム管理者は営業部IDが不要
     * システム管理者（SYSTEM_ADMINISTRATOR）は営業部IDがnullでも作成可能
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: システム管理者の営業部ID不要")
    void systemAdministratorDoesNotRequireDepartmentId(@ForAll("validUserNames") UserName userName) {
        UserId userId = UserId.generate();
        
        // システム管理者は営業部IDがnullでも作成可能
        User admin = User.create(userId, userName, Role.SYSTEM_ADMINISTRATOR, null);
        assertThat(admin.getRole()).isEqualTo(Role.SYSTEM_ADMINISTRATOR);
        assertThat(admin.getDepartmentId()).isNull();
        
        // システム管理者は営業部IDがあっても作成可能
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        User adminWithDept = User.create(UserId.generate(), userName, Role.SYSTEM_ADMINISTRATOR, departmentId);
        assertThat(adminWithDept.getRole()).isEqualTo(Role.SYSTEM_ADMINISTRATOR);
        assertThat(adminWithDept.getDepartmentId()).isEqualTo(departmentId);
    }

    /**
     * プロパティ2: 役割に応じた権限の整合性
     * 各役割は適切な権限を持つ
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: 役割に応じた権限の整合性")
    void roleBasedPermissionsAreConsistent(@ForAll("validUsers") User user,
                                         @ForAll("validCustomerIds") CustomerId customerId) {
        Role role = user.getRole();
        
        // 営業担当者の権限
        if (role == Role.SALES_REPRESENTATIVE) {
            assertThat(user.canApproveCustomerChange(customerId)).isFalse();
            assertThat(user.canApproveAssignmentChange()).isFalse();
            assertThat(user.canDirectlyUpdateCustomer(customerId)).isFalse();
        }
        
        // 部長の権限
        if (role == Role.DEPARTMENT_MANAGER) {
            assertThat(user.canApproveCustomerChange(customerId)).isTrue();
            assertThat(user.canApproveAssignmentChange()).isTrue();
            assertThat(user.canDirectlyUpdateCustomer(customerId)).isFalse();
        }
        
        // システム管理者の権限
        if (role == Role.SYSTEM_ADMINISTRATOR) {
            assertThat(user.canApproveCustomerChange(customerId)).isTrue();
            assertThat(user.canApproveAssignmentChange()).isTrue();
            assertThat(user.canDirectlyUpdateCustomer(customerId)).isTrue();
        }
    }

    /**
     * プロパティ2: アカウントロック機能の整合性
     * ログイン失敗回数に応じてアカウントが適切にロックされる
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: アカウントロック機能の整合性")
    void accountLockingIsConsistent(@ForAll("validUsers") User user) {
        // 初期状態ではロックされていない
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.canUseSystem()).isTrue();
        
        // 2回失敗してもロックされない
        user.recordLoginFailure();
        user.recordLoginFailure();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.canUseSystem()).isTrue();
        
        // 3回目の失敗でロックされる
        user.recordLoginFailure();
        assertThat(user.isAccountLocked()).isTrue();
        assertThat(user.canUseSystem()).isFalse();
        
        // ログイン成功でロック解除
        user.recordSuccessfulLogin();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.canUseSystem()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    }

    /**
     * プロパティ2: ユーザー状態の整合性
     * ユーザーの状態変更が適切に動作する
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: ユーザー状態の整合性")
    void userStatusIsConsistent(@ForAll("validUsers") User user) {
        // 初期状態はACTIVE
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.canUseSystem()).isTrue();
        
        // 無効化
        user.deactivate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.canUseSystem()).isFalse();
        
        // 有効化
        user.activate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.canUseSystem()).isTrue();
        
        // 削除（論理削除）
        user.delete();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.canUseSystem()).isFalse();
    }

    /**
     * プロパティ2: 役割変更の整合性
     * 役割変更時に営業部IDの制約が適切に適用される
     */
    @Property
    @Label("Feature: customer-master-system, Property 2: 役割変更の整合性")
    void roleChangeIsConsistent(@ForAll("validUsers") User user) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        
        // システム管理者から営業担当者への変更（営業部ID必須）
        if (user.getRole() == Role.SYSTEM_ADMINISTRATOR) {
            user.changeRole(Role.SALES_REPRESENTATIVE, departmentId);
            assertThat(user.getRole()).isEqualTo(Role.SALES_REPRESENTATIVE);
            assertThat(user.getDepartmentId()).isEqualTo(departmentId);
            
            // 営業担当者からシステム管理者への変更（営業部ID不要）
            user.changeRole(Role.SYSTEM_ADMINISTRATOR, null);
            assertThat(user.getRole()).isEqualTo(Role.SYSTEM_ADMINISTRATOR);
            assertThat(user.getDepartmentId()).isNull();
        }
        
        // 営業担当者から部長への変更
        if (user.getRole() == Role.SALES_REPRESENTATIVE) {
            user.changeRole(Role.DEPARTMENT_MANAGER, user.getDepartmentId());
            assertThat(user.getRole()).isEqualTo(Role.DEPARTMENT_MANAGER);
            assertThat(user.getDepartmentId()).isNotNull();
        }
    }

    // テストデータ生成用のArbitrary

    @Provide
    Arbitrary<UserName> validUserNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(20)
            .map(UserName::of);
    }

    @Provide
    Arbitrary<User> validUsers() {
        return Combinators.combine(
            Arbitraries.of(Role.values()),
            validUserNames()
        ).as((role, userName) -> {
            UserId userId = UserId.generate();
            SalesDepartmentId departmentId = role.isSalesRepresentative() ? 
                SalesDepartmentId.generate() : null;
            return User.create(userId, userName, role, departmentId);
        });
    }

    @Provide
    Arbitrary<CustomerId> validCustomerIds() {
        return Arbitraries.create(() -> CustomerId.generate());
    }
}