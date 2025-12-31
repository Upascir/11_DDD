package com.customermaster.domain.salesdepartment;

import com.customermaster.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

/**
 * SalesDepartment集約の単体テスト
 */
class SalesDepartmentTest {

    @Test
    @DisplayName("新規営業部を作成できる")
    void canCreateNewSalesDepartment() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        DepartmentName departmentName = DepartmentName.of("第一営業部");
        UserId initialManager = UserId.generate();
        
        // When
        SalesDepartment department = SalesDepartment.create(departmentId, departmentName, initialManager);
        
        // Then
        assertThat(department.getId()).isEqualTo(departmentId);
        assertThat(department.getDepartmentName()).isEqualTo(departmentName);
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(1);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        assertThat(department.isSalesRepresentative(initialManager)).isTrue();
        assertThat(department.isDepartmentManager(initialManager)).isTrue();
    }

    @Test
    @DisplayName("営業担当者を追加できる")
    void canAddSalesRepresentative() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId newSalesRep = UserId.generate();
        
        // When
        department.addSalesRepresentative(newSalesRep);
        
        // Then
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(2);
        assertThat(department.isSalesRepresentative(newSalesRep)).isTrue();
        assertThat(department.isDepartmentManager(newSalesRep)).isFalse();
    }

    @Test
    @DisplayName("営業担当者を部長に任命できる")
    void canPromoteSalesRepresentativeToDepartmentManager() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId salesRep = UserId.generate();
        department.addSalesRepresentative(salesRep);
        
        // When
        department.addDepartmentManager(salesRep);
        
        // Then
        assertThat(department.getDepartmentManagerCount()).isEqualTo(2);
        assertThat(department.isDepartmentManager(salesRep)).isTrue();
        assertThat(department.isSalesRepresentative(salesRep)).isTrue();
    }

    @Test
    @DisplayName("営業担当者でないユーザーを部長に任命しようとすると例外が発生する")
    void throwsExceptionWhenPromotingNonSalesRepresentativeToDepartmentManager() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId nonSalesRep = UserId.generate();
        
        // When & Then
        assertThatThrownBy(() -> department.addDepartmentManager(nonSalesRep))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("部長は営業担当者の中から選出される必要があります");
    }

    @Test
    @DisplayName("部長を営業担当者から削除しようとすると例外が発生する")
    void throwsExceptionWhenRemovingDepartmentManagerFromSalesRepresentatives() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId manager = department.getDepartmentManagers().iterator().next();
        
        // When & Then
        assertThatThrownBy(() -> department.removeSalesRepresentative(manager))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("部長は営業担当者から削除する前に部長の役割を解除してください");
    }

    @Test
    @DisplayName("最後の部長を削除しようとすると例外が発生する")
    void throwsExceptionWhenRemovingLastDepartmentManager() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId manager = department.getDepartmentManagers().iterator().next();
        
        // When & Then
        assertThatThrownBy(() -> department.removeDepartmentManager(manager))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("営業部には最低1人の部長が必要です");
    }

    @Test
    @DisplayName("複数の部長がいる場合は部長を削除できる")
    void canRemoveDepartmentManagerWhenMultipleManagersExist() {
        // Given
        SalesDepartment department = createTestDepartment();
        UserId newSalesRep = UserId.generate();
        department.addSalesRepresentative(newSalesRep);
        department.addDepartmentManager(newSalesRep);
        
        // When
        department.removeDepartmentManager(newSalesRep);
        
        // Then
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        assertThat(department.isDepartmentManager(newSalesRep)).isFalse();
        assertThat(department.isSalesRepresentative(newSalesRep)).isTrue();
    }

    @Test
    @DisplayName("営業部を非アクティブ化すると新しいメンバーを追加できない")
    void cannotAddMembersWhenDepartmentIsInactive() {
        // Given
        SalesDepartment department = createTestDepartment();
        department.deactivate();
        UserId newSalesRep = UserId.generate();
        
        // When & Then
        assertThatThrownBy(() -> department.addSalesRepresentative(newSalesRep))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には営業担当者を追加できません");
            
        assertThatThrownBy(() -> department.addDepartmentManager(newSalesRep))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("非アクティブな営業部には部長を追加できません");
    }

    @Test
    @DisplayName("営業部の状態を変更できる")
    void canChangeDepartmentStatus() {
        // Given
        SalesDepartment department = createTestDepartment();
        
        // When & Then
        assertThat(department.isActive()).isTrue();
        
        department.deactivate();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.INACTIVE);
        assertThat(department.isActive()).isFalse();
        
        department.activate();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
        assertThat(department.isActive()).isTrue();
        
        department.delete();
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.DELETED);
        assertThat(department.isActive()).isFalse();
    }

    @Test
    @DisplayName("部署名を変更できる")
    void canChangeDepartmentName() {
        // Given
        SalesDepartment department = createTestDepartment();
        DepartmentName newName = DepartmentName.of("第二営業部");
        
        // When
        department.changeDepartmentName(newName);
        
        // Then
        assertThat(department.getDepartmentName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("既存営業部を復元できる")
    void canRestoreExistingSalesDepartment() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        DepartmentName departmentName = DepartmentName.of("復元営業部");
        Set<UserId> salesReps = Set.of(UserId.generate(), UserId.generate());
        Set<UserId> managers = Set.of(salesReps.iterator().next());
        LocalDateTime now = LocalDateTime.now();
        
        // When
        SalesDepartment department = SalesDepartment.restore(
            departmentId, departmentName, salesReps, managers,
            DepartmentStatus.ACTIVE, now, now
        );
        
        // Then
        assertThat(department.getId()).isEqualTo(departmentId);
        assertThat(department.getDepartmentName()).isEqualTo(departmentName);
        assertThat(department.getSalesRepresentativeCount()).isEqualTo(2);
        assertThat(department.getDepartmentManagerCount()).isEqualTo(1);
        assertThat(department.getStatus()).isEqualTo(DepartmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("不変条件に違反する営業部の復元は失敗する")
    void failsToRestoreDepartmentWithInvariantViolation() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        DepartmentName departmentName = DepartmentName.of("不正営業部");
        Set<UserId> salesReps = Set.of(UserId.generate());
        Set<UserId> managers = Set.of(UserId.generate()); // 営業担当者にいないユーザーを部長に設定
        LocalDateTime now = LocalDateTime.now();
        
        // When & Then
        assertThatThrownBy(() -> SalesDepartment.restore(
            departmentId, departmentName, salesReps, managers,
            DepartmentStatus.ACTIVE, now, now
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("部長は営業担当者の中から選出される必要があります");
    }

    private SalesDepartment createTestDepartment() {
        return SalesDepartment.create(
            SalesDepartmentId.generate(),
            DepartmentName.of("テスト営業部"),
            UserId.generate()
        );
    }
}