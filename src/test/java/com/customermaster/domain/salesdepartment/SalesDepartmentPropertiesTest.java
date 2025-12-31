package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.user.UserId;
import net.jqwik.api.*;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SalesDepartment集約のプロパティベーステスト
 * 
 * プロパティ3: 営業部の組織整合性
 * 検証要件: 1.1, 1.7
 */
class SalesDepartmentPropertiesTest {

    /**
     * プロパティ3: 営業部には最低1人の部長が必要
     * 営業部の不変条件として、常に最低1人の部長が設定されている必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部の最低部長数制約")
    void salesDepartmentMustHaveAtLeastOneManager(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        
        // 新規作成時は初期部長が必須
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        assertThat(department.isDepartmentManager(managerId)).isTrue();
        assertThat(department.isSalesRepresentative(managerId)).isTrue();
        
        // 複数部長がいる場合、最後の1人は削除できない
        UserId secondManagerId = UserId.generate();
        department.addSalesRepresentative(secondManagerId);
        department.addDepartmentManager(secondManagerId);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(2);
        
        // 1人目の部長を削除
        department.removeDepartmentManager(managerId);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        assertThat(department.isDepartmentManager(secondManagerId)).isTrue();
        
        // 最後の部長は削除できない
        assertThatThrownBy(() -> department.removeDepartmentManager(secondManagerId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("営業部には最低1人の部長が必要です");
    }

    /**
     * プロパティ3: 部長は営業担当者の中から選出される
     * 部長に任命されるユーザーは、事前に営業担当者として登録されている必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 部長の営業担当者選出制約")
    void departmentManagerMustBeSalesRepresentative(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId initialManagerId = UserId.generate();
        UserId salesRepId = UserId.generate();
        UserId outsiderId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, initialManagerId);
        
        // 営業担当者を追加
        department.addSalesRepresentative(salesRepId);
        
        // 営業担当者は部長に任命可能
        department.addDepartmentManager(salesRepId);
        assertThat(department.isDepartmentManager(salesRepId)).isTrue();
        
        // 営業担当者でないユーザーは部長に任命不可
        assertThatThrownBy(() -> department.addDepartmentManager(outsiderId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("部長は営業担当者の中から選出される必要があります");
    }

    /**
     * プロパティ3: 部長の営業担当者削除制約
     * 部長として任命されているユーザーは、部長の役割を解除するまで営業担当者から削除できない
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 部長の営業担当者削除制約")
    void cannotRemoveSalesRepresentativeWhoIsManager(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        
        // 部長は営業担当者から直接削除できない
        assertThatThrownBy(() -> department.removeSalesRepresentative(managerId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("部長は営業担当者から削除する前に部長の役割を解除してください");
        
        // 別の部長を追加してから元の部長を解除すれば削除可能
        UserId secondManagerId = UserId.generate();
        department.addSalesRepresentative(secondManagerId);
        department.addDepartmentManager(secondManagerId);
        department.removeDepartmentManager(managerId);
        
        // 部長でなくなったので営業担当者から削除可能
        department.removeSalesRepresentative(managerId);
        assertThat(department.isSalesRepresentative(managerId)).isFalse();
        assertThat(department.isDepartmentManager(managerId)).isFalse();
    }

    /**
     * プロパティ3: 営業部の状態遷移の整合性
     * 営業部の状態（ACTIVE/INACTIVE/DELETED）が適切に管理される
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部の状態遷移整合性")
    void departmentStatusTransitionIsConsistent(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        
        // 初期状態はACTIVE
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        assertThat(department.isActive()).isTrue();
        
        // 非アクティブ化
        department.deactivate();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.INACTIVE);
        assertThat(department.isActive()).isFalse();
        
        // 再アクティブ化
        department.activate();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        assertThat(department.isActive()).isTrue();
        
        // 削除（論理削除）
        department.delete();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.DELETED);
        assertThat(department.isActive()).isFalse();
    }

    /**
     * プロパティ3: 非アクティブ営業部への操作制限
     * 非アクティブな営業部には営業担当者や部長を追加できない
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 非アクティブ営業部への操作制限")
    void inactiveDepartmentRestrictsOperations(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        UserId newUserId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        
        // 非アクティブ化
        department.deactivate();
        
        // 非アクティブな営業部には営業担当者を追加できない
        assertThatThrownBy(() -> department.addSalesRepresentative(newUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には営業担当者を追加できません");
        
        // 非アクティブな営業部には部長を追加できない
        assertThatThrownBy(() -> department.addDepartmentManager(newUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には部長を追加できません");
    }

    /**
     * プロパティ3: 営業部のメンバー管理の整合性
     * 営業担当者と部長の追加・削除が適切に管理される
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部のメンバー管理整合性")
    void memberManagementIsConsistent(@ForAll("validDepartmentNames") DepartmentName departmentName,
                                    @ForAll("userIdSets") Set<UserId> userIds) {
        Assume.that(userIds.size() >= 2); // 最低2人のユーザーが必要
        
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId[] userArray = userIds.toArray(new UserId[0]);
        UserId initialManagerId = userArray[0];
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, initialManagerId);
        
        // 複数の営業担当者を追加
        for (int i = 1; i < userArray.length; i++) {
            department.addSalesRepresentative(userArray[i]);
        }
        
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(userIds.size());
        
        // 一部を部長に任命
        for (int i = 1; i < Math.min(3, userArray.length); i++) {
            department.addDepartmentManager(userArray[i]);
        }
        
        // 部長数の確認
        int expectedManagerCount = Math.min(3, userArray.length);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(expectedManagerCount);
        
        // 全ての部長は営業担当者でもある
        for (UserId managerId : department.getDepartmentManagers()) {
            assertThat(department.isSalesRepresentative(managerId)).isTrue();
        }
    }

    // テストデータ生成用のArbitrary

    @Provide
    Arbitrary<DepartmentName> validDepartmentNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(20)
            .map(DepartmentName::of);
    }

    @Provide
    Arbitrary<Set<UserId>> userIdSets() {
        return Arbitraries.integers()
            .between(2, 5)
            .flatMap(size -> 
                Arbitraries.create(() -> {
                    Set<UserId> userIds = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        userIds.add(UserId.generate());
                    }
                    return userIds;
                })
            );
    }
}