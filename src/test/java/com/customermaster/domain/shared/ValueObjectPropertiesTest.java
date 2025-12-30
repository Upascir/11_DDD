package com.customermaster.domain.shared;

import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.customer.CustomerName;
import com.customermaster.domain.salesdepartment.DepartmentName;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.user.UserId;
import com.customermaster.domain.user.UserName;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 値オブジェクトのプロパティベーステスト
 * 
 * プロパティ1: 値オブジェクトの不変性
 * 検証要件: 2.1-2.8
 */
class ValueObjectPropertiesTest {

    /**
     * プロパティ1: EntityIDの不変性と等価性
     * 同じ値から作成されたEntityIDは等価であり、異なる値から作成されたものは非等価である
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: EntityID不変性と等価性")
    void entityIdImmutabilityAndEquality(@ForAll @NotBlank String value1, 
                                        @ForAll @NotBlank String value2) {
        Assume.that(!value1.trim().equals(value2.trim()));
        
        // CustomerId
        CustomerId customerId1a = CustomerId.of(value1);
        CustomerId customerId1b = CustomerId.of(value1);
        CustomerId customerId2 = CustomerId.of(value2);
        
        // 同じ値から作成されたIDは等価
        assertThat(customerId1a).isEqualTo(customerId1b);
        assertThat(customerId1a.hashCode()).isEqualTo(customerId1b.hashCode());
        
        // 異なる値から作成されたIDは非等価
        assertThat(customerId1a).isNotEqualTo(customerId2);
        
        // 値は不変
        assertThat(customerId1a.getValue()).isEqualTo(value1.trim());
        
        // UserId
        UserId userId1a = UserId.of(value1);
        UserId userId1b = UserId.of(value1);
        UserId userId2 = UserId.of(value2);
        
        assertThat(userId1a).isEqualTo(userId1b);
        assertThat(userId1a.hashCode()).isEqualTo(userId1b.hashCode());
        assertThat(userId1a).isNotEqualTo(userId2);
        assertThat(userId1a.getValue()).isEqualTo(value1.trim());
        
        // SalesDepartmentId
        SalesDepartmentId deptId1a = SalesDepartmentId.of(value1);
        SalesDepartmentId deptId1b = SalesDepartmentId.of(value1);
        SalesDepartmentId deptId2 = SalesDepartmentId.of(value2);
        
        assertThat(deptId1a).isEqualTo(deptId1b);
        assertThat(deptId1a.hashCode()).isEqualTo(deptId1b.hashCode());
        assertThat(deptId1a).isNotEqualTo(deptId2);
        assertThat(deptId1a.getValue()).isEqualTo(value1.trim());
    }

    /**
     * プロパティ1: 名前値オブジェクトの不変性と等価性
     * 同じ値から作成された名前は等価であり、異なる値から作成されたものは非等価である
     */
    @Property
    @Label("Feature: customer-master-system, Property 1: 名前値オブジェクト不変性と等価性")
    void nameValueObjectImmutabilityAndEquality(@ForAll @NotBlank @StringLength(max = 50) String name1,
                                               @ForAll @NotBlank @StringLength(max = 50) String name2) {
        Assume.that(!name1.trim().equals(name2.trim()));
        
        // CustomerName (max 100文字だが、テストでは50文字に制限)
        CustomerName customerName1a = CustomerName.of(name1);
        CustomerName customerName1b = CustomerName.of(name1);
        CustomerName customerName2 = CustomerName.of(name2);
        
        // 同じ値から作成された名前は等価
        assertThat(customerName1a).isEqualTo(customerName1b);
        assertThat(customerName1a.hashCode()).isEqualTo(customerName1b.hashCode());
        
        // 異なる値から作成された名前は非等価
        assertThat(customerName1a).isNotEqualTo(customerName2);
        
        // 値は不変（トリムされる）
        assertThat(customerName1a.getValue()).isEqualTo(name1.trim());
        
        // UserName
        UserName userName1a = UserName.of(name1);
        UserName userName1b = UserName.of(name1);
        UserName userName2 = UserName.of(name2);
        
        assertThat(userName1a).isEqualTo(userName1b);
        assertThat(userName1a.hashCode()).isEqualTo(userName1b.hashCode());
        assertThat(userName1a).isNotEqualTo(userName2);
        assertThat(userName1a.getValue()).isEqualTo(name1.trim());
        
        // DepartmentName
        DepartmentName deptName1a = DepartmentName.of(name1);
        DepartmentName deptName1b = DepartmentName.of(name1);
        DepartmentName deptName2 = DepartmentName.of(name2);
        
        assertThat(deptName1a).isEqualTo(deptName1b);
        assertThat(deptName1a.hashCode()).isEqualTo(deptName1b.hashCode());
        assertThat(deptName1a).isNotEqualTo(deptName2);
        assertThat(deptName1a.getValue()).isEqualTo(name1.trim());
    }

    /**
     * プロパティ1: 生成されたIDの一意性
     * generate()メソッドで生成されるIDは常に一意である
     */
    @Property(tries = 1000)
    @Label("Feature: customer-master-system, Property 1: 生成IDの一意性")
    void generatedIdsAreUnique() {
        // 複数のIDを生成して一意性を確認
        CustomerId customerId1 = CustomerId.generate();
        CustomerId customerId2 = CustomerId.generate();
        UserId userId1 = UserId.generate();
        UserId userId2 = UserId.generate();
        SalesDepartmentId deptId1 = SalesDepartmentId.generate();
        SalesDepartmentId deptId2 = SalesDepartmentId.generate();
        
        // 生成されたIDは全て異なる
        assertThat(customerId1).isNotEqualTo(customerId2);
        assertThat(userId1).isNotEqualTo(userId2);
        assertThat(deptId1).isNotEqualTo(deptId2);
        
        // 値も異なる
        assertThat(customerId1.getValue()).isNotEqualTo(customerId2.getValue());
        assertThat(userId1.getValue()).isNotEqualTo(userId2.getValue());
        assertThat(deptId1.getValue()).isNotEqualTo(deptId2.getValue());
        
        // 値は空でない
        assertThat(customerId1.getValue()).isNotBlank();
        assertThat(userId1.getValue()).isNotBlank();
        assertThat(deptId1.getValue()).isNotBlank();
    }
}