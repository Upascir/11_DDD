package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.user.UserId;
import net.jqwik.api.*;

import java.time.LocalDateTime;
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
     * 営業部を作成・復元する際、必ず1人以上の部長が設定されている必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部の最低部長数制約")
    void salesDepartmentRequiresAtLeastOneManager(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        
        // 新規作成時は初期部長が必須
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        assertThat(department.getDepartmentManagerCount()).isGreaterThanOrEqualTo(1);
        assertThat(department.isDepartmentManager(managerId)).isTrue();
        assertThat(department.isSalesRepresentative(managerId)).isTrue();
        
        // 部長を追加
        UserId additionalManagerId = UserId.generate();
        department.addSalesRepresentative(additionalManagerId);
        department.addDepartmentManager(additionalManagerId);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(2);
        
        // 最後の部長は削除できない
        department.removeDepartmentManager(additionalManagerId);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        
        assertThatThrownBy(() -> department.removeDepartmentManager(managerId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("営業部には最低1人の部長が必要です");
    }

    /**
     * プロパティ3: 部長は営業担当者から選出される
     * 部長に任命されるユーザーは必ず営業担当者である必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 部長の営業担当者選出制約")
    void departmentManagerMustBeSalesRepresentative(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId initialManagerId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, initialManagerId);
        
        // 営業担当者でないユーザーを部長に任命しようとすると例外
        UserId nonSalesRepId = UserId.generate();
        assertThatThrownBy(() -> department.addDepartmentManager(nonSalesRepId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("部長は営業担当者の中から選出される必要があります");
        
        // 営業担当者を追加してから部長に任命
        department.addSalesRepresentative(nonSalesRepId);
        department.addDepartmentManager(nonSalesRepId);
        assertThat(department.isDepartmentManager(nonSalesRepId)).isTrue();
        assertThat(department.isSalesRepresentative(nonSalesRepId)).isTrue();
    }

    /**
     * プロパティ3: 部長は営業担当者から削除する前に部長の役割を解除する必要がある
     * 部長を営業担当者から削除する場合、先に部長の役割を解除する必要がある
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 部長の営業担当者削除制約")
    void managerMustBeRemovedFromManagerRoleFirst(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId1 = UserId.generate();
        UserId managerId2 = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId1);
        
        // 2人目の部長を追加
        department.addSalesRepresentative(managerId2);
        department.addDepartmentManager(managerId2);
        
        // 部長のまま営業担当者から削除しようとすると例外
        assertThatThrownBy(() -> department.removeSalesRepresentative(managerId2))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("部長は営業担当者から削除する前に部長の役割を解除してください");
        
        // 部長の役割を解除してから営業担当者から削除
        department.removeDepartmentManager(managerId2);
        department.removeSalesRepresentative(managerId2);
        assertThat(department.isSalesRepresentative(managerId2)).isFalse();
        assertThat(department.isDepartmentManager(managerId2)).isFalse();
    }

    /**
     * プロパティ3: 営業部の状態管理の整合性
     * 営業部の状態変更が適切に動作する
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部状態管理の整合性")
    void departmentStatusManagementIsConsistent(@ForAll("validDepartmentNames") DepartmentName departmentName) {
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
        
        // 非アクティブ状態では営業担当者・部長を追加できない
        UserId newUserId = UserId.generate();
        assertThatThrownBy(() -> department.addSalesRepresentative(newUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には営業担当者を追加できません");
        
        assertThatThrownBy(() -> department.addDepartmentManager(newUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には部長を追加できません");
        
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
     * プロパティ3: 営業部の復元時の不変条件検証
     * 既存データから営業部を復元する際、不変条件が適切に検証される
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部復元時の不変条件検証")
    void departmentRestorationValidatesInvariants(@ForAll("validDepartmentNames") DepartmentName departmentName,
                                                @ForAll("validUserIdSets") Set<UserId> salesReps,
                                                @ForAll("validUserIdSets") Set<UserId> managers) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        LocalDateTime now = LocalDateTime.now();
        
        // 部長が空の場合は復元時に例外
        if (managers.isEmpty()) {
            assertThatThrownBy(() -> 
                SalesDepartment.restore(departmentId, departmentName, salesReps, managers, 
                                      DepartmentStatus.ACTIVE, now, now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("営業部には最低1人の部長が必要です");
            return;
        }
        
        // 部長が営業担当者に含まれていない場合は復元時に例外
        boolean allManagersAreSalesReps = salesReps.containsAll(managers);
        if (!allManagersAreSalesReps) {
            assertThatThrownBy(() -> 
                SalesDepartment.restore(departmentId, departmentName, salesReps, managers, 
                                      DepartmentStatus.ACTIVE, now, now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("部長は営業担当者の中から選出される必要があります");
            return;
        }
        
        // 正常な場合は復元成功
        SalesDepartment department = SalesDepartment.restore(departmentId, departmentName, 
                                                           salesReps, managers, 
                                                           DepartmentStatus.ACTIVE, now, now);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(managers.size());
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(salesReps.size());
        
        // 復元後も不変条件が維持される
        for (UserId managerId : managers) {
            assertThat(department.isDepartmentManager(managerId)).isTrue();
            assertThat(department.isSalesRepresentative(managerId)).isTrue();
        }
    }

    /**
     * プロパティ3: 営業部の組織変更操作の冪等性
     * 同じ操作を複数回実行しても結果が変わらない
     */
    @Property
    @Label("Feature: customer-master-system, Property 3: 営業部組織変更の冪等性")
    void departmentOperationsAreIdempotent(@ForAll("validDepartmentNames") DepartmentName departmentName) {
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        UserId managerId = UserId.generate();
        UserId salesRepId = UserId.generate();
        
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, managerId);
        
        // 営業担当者の追加は冪等
        department.addSalesRepresentative(salesRepId);
        int initialSalesRepCount = department.getSalesRepresentativeCount();
        department.addSalesRepresentative(salesRepId); // 同じユーザーを再度追加
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(initialSalesRepCount);
        
        // 部長の追加は冪等
        department.addDepartmentManager(salesRepId);
        int initialManagerCount = department.getDepartmentManagerCount();
        department.addDepartmentManager(salesRepId); // 同じユーザーを再度追加
        assertThat(department.getDepartmentManagerCount()).isEqualTo(initialManagerCount);
        
        // 削除操作も冪等
        department.removeDepartmentManager(salesRepId);
        department.removeSalesRepresentative(salesRepId);
        int finalSalesRepCount = department.getSalesRepresentativeCount();
        int finalManagerCount = department.getDepartmentManagerCount();
        
        // 存在しないユーザーの削除は何も起こらない
        department.removeSalesRepresentative(salesRepId);
        department.removeDepartmentManager(salesRepId);
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(finalSalesRepCount);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(finalManagerCount);
    }

    // テストデータ生成用のArbitrary

    @Provide
    Arbitrary<DepartmentName> validDepartmentNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(20)
            .map(name -> DepartmentName.of("営業" + name + "部"));
    }

    @Provide
    Arbitrary<Set<UserId>> validUserIdSets() {
        return Arbitraries.integers()
            .between(0, 5)
            .flatMap(size -> {
                if (size == 0) {
                    return Arbitraries.just(new HashSet<>());
                }
                return Arbitraries.create(() -> {
                    Set<UserId> userIds = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        userIds.add(UserId.generate());
                    }
                    return userIds;
                });
            });
    }
}