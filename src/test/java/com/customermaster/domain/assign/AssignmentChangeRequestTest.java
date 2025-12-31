package com.customermaster.domain.assign;

import com.customermaster.domain.user.UserId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * AssignmentChangeRequest の単体テスト
 */
class AssignmentChangeRequestTest {

    @Test
    @DisplayName("担当者変更申請を作成できる")
    void canCreateAssignmentChangeRequest() {
        // Given
        UserId requesterId = UserId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());
        UserId newSalesRepId = UserId.generate();
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();
        String reason = "担当者変更のため";

        // When
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            requesterId, currentAssignment, newSalesRepId, newDepartmentId, reason);

        // Then
        assertThat(request.getRequesterId()).isEqualTo(requesterId);
        assertThat(request.getCurrentAssignment()).isEqualTo(currentAssignment);
        assertThat(request.getNewAssignment().getSalesRepresentativeId()).isEqualTo(newSalesRepId);
        assertThat(request.getNewAssignment().getDepartmentId()).isEqualTo(newDepartmentId);
        assertThat(request.getReason()).isEqualTo(reason);
        assertThat(request.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("営業部内変更の場合、変更種類がWITHIN_DEPARTMENTになる")
    void changeTypeIsWithinDepartmentForSameDepartment() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId);
        UserId newSalesRepId = UserId.generate();

        // When
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, newSalesRepId, departmentId, "理由");

        // Then
        assertThat(request.getChangeType()).isEqualTo(AssignmentChangeType.WITHIN_DEPARTMENT);
    }

    @Test
    @DisplayName("営業部間変更の場合、変更種類がCROSS_DEPARTMENTになる")
    void changeTypeIsCrossDepartmentForDifferentDepartment() {
        // Given
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();

        // When
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, UserId.generate(), newDepartmentId, "理由");

        // Then
        assertThat(request.getChangeType()).isEqualTo(AssignmentChangeType.CROSS_DEPARTMENT);
    }

    @Test
    @DisplayName("変更理由が空文字列の場合、例外が発生する")
    void throwsExceptionWhenReasonIsEmpty() {
        // Given
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());

        // When & Then
        assertThatThrownBy(() -> AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, UserId.generate(), SalesDepartmentId.generate(), ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("変更理由は必須です");
    }

    @Test
    @DisplayName("変更理由が空白のみの場合、例外が発生する")
    void throwsExceptionWhenReasonIsBlank() {
        // Given
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());

        // When & Then
        assertThatThrownBy(() -> AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, UserId.generate(), SalesDepartmentId.generate(), "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("変更理由は必須です");
    }

    @Test
    @DisplayName("同じ担当者への変更の場合、例外が発生する")
    void throwsExceptionWhenChangingToSameRepresentative() {
        // Given
        UserId salesRepId = UserId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            salesRepId, SalesDepartmentId.generate());

        // When & Then
        assertThatThrownBy(() -> AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, salesRepId, SalesDepartmentId.generate(), "理由"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("同じ担当者への変更はできません");
    }

    @Test
    @DisplayName("営業部内変更の場合、承認が必要な営業部は現在の営業部のみ")
    void requiredApprovalDepartmentsForWithinDepartmentChange() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, UserId.generate(), departmentId, "理由");

        // When
        SalesDepartmentId[] requiredDepartments = request.getRequiredApprovalDepartments();

        // Then
        assertThat(requiredDepartments).hasSize(1);
        assertThat(requiredDepartments[0]).isEqualTo(departmentId);
    }

    @Test
    @DisplayName("営業部間変更の場合、承認が必要な営業部は現在と新しい営業部の両方")
    void requiredApprovalDepartmentsForCrossDepartmentChange() {
        // Given
        SalesDepartmentId currentDepartmentId = SalesDepartmentId.generate();
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), currentDepartmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            UserId.generate(), currentAssignment, UserId.generate(), newDepartmentId, "理由");

        // When
        SalesDepartmentId[] requiredDepartments = request.getRequiredApprovalDepartments();

        // Then
        assertThat(requiredDepartments).hasSize(2);
        assertThat(requiredDepartments).containsExactlyInAnyOrder(currentDepartmentId, newDepartmentId);
    }
}