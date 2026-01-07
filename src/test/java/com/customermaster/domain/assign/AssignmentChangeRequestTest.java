package com.customermaster.domain.assign;

import com.customermaster.domain.user.UserId;
import com.customermaster.domain.customer.CustomerId;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * AssignmentChangeRequest の単体テスト
 */
class AssignmentChangeRequestTest {

    @Test
    @DisplayName("担当者変更申請を作成できる")
    void canCreateAssignmentChangeRequest() {
        // Given
        CustomerId customerId = CustomerId.generate();
        UserId requesterId = UserId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), SalesDepartmentId.generate());
        UserId newSalesRepId = UserId.generate();
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();
        String reason = "担当者変更のため";

        // When
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            customerId, requesterId, currentAssignment, newSalesRepId, newDepartmentId, reason);

        // Then
        assertThat(request.getId()).isNotNull();
        assertThat(request.getCustomerId()).isEqualTo(customerId);
        assertThat(request.getRequesterId()).isEqualTo(requesterId);
        assertThat(request.getCurrentAssignment()).isEqualTo(currentAssignment);
        assertThat(request.getNewAssignment().getSalesRepresentativeId()).isEqualTo(newSalesRepId);
        assertThat(request.getNewAssignment().getDepartmentId()).isEqualTo(newDepartmentId);
        assertThat(request.getReason()).isEqualTo(reason);
        assertThat(request.getRequestedAt()).isNotNull();
        assertThat(request.getDeadline()).isNotNull();
        assertThat(request.getStatus()).isEqualTo(AssignmentApprovalStatus.PENDING);
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
            CustomerId.generate(), UserId.generate(), currentAssignment, newSalesRepId, departmentId, "理由");

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
            CustomerId.generate(), UserId.generate(), currentAssignment, UserId.generate(), newDepartmentId, "理由");

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
            CustomerId.generate(), UserId.generate(), currentAssignment, UserId.generate(), SalesDepartmentId.generate(), ""))
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
            CustomerId.generate(), UserId.generate(), currentAssignment, UserId.generate(), SalesDepartmentId.generate(), "   "))
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
            CustomerId.generate(), UserId.generate(), currentAssignment, salesRepId, SalesDepartmentId.generate(), "理由"))
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
            CustomerId.generate(), UserId.generate(), currentAssignment, UserId.generate(), departmentId, "理由");

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
            CustomerId.generate(), UserId.generate(), currentAssignment, UserId.generate(), newDepartmentId, "理由");

        // When
        SalesDepartmentId[] requiredDepartments = request.getRequiredApprovalDepartments();

        // Then
        assertThat(requiredDepartments).hasSize(2);
        assertThat(requiredDepartments).containsExactlyInAnyOrder(currentDepartmentId, newDepartmentId);
    }

    @Test
    @DisplayName("担当者変更申請を承認できる")
    void canApproveAssignmentChangeRequest() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            CustomerId.generate(), UserId.generate(), currentAssignment, 
            UserId.generate(), departmentId, "理由");
        
        UserId approverId = UserId.generate();
        String comment = "承認します";

        // When
        request.approve(approverId, departmentId, comment);

        // Then
        assertThat(request.getStatus()).isEqualTo(AssignmentApprovalStatus.APPROVED);
        assertThat(request.getApprovals()).hasSize(1);
        assertThat(request.getApprovals().get(0).getApproverId()).isEqualTo(approverId);
        assertThat(request.getApprovals().get(0).getComment()).isEqualTo(comment);
        assertThat(request.getApprovals().get(0).isApproval()).isTrue();
    }

    @Test
    @DisplayName("担当者変更申請を却下できる")
    void canRejectAssignmentChangeRequest() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            CustomerId.generate(), UserId.generate(), currentAssignment, 
            UserId.generate(), departmentId, "理由");
        
        UserId approverId = UserId.generate();
        String reason = "却下理由";

        // When
        request.reject(approverId, departmentId, reason);

        // Then
        assertThat(request.getStatus()).isEqualTo(AssignmentApprovalStatus.REJECTED);
        assertThat(request.getApprovals()).hasSize(1);
        assertThat(request.getApprovals().get(0).getApproverId()).isEqualTo(approverId);
        assertThat(request.getApprovals().get(0).getComment()).isEqualTo(reason);
        assertThat(request.getApprovals().get(0).isApproval()).isFalse();
    }

    @Test
    @DisplayName("営業部間変更で一方の営業部が承認した場合、部分承認状態になる")
    void partiallyApprovedForCrossDepartmentChange() {
        // Given
        SalesDepartmentId currentDepartmentId = SalesDepartmentId.generate();
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), currentDepartmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            CustomerId.generate(), UserId.generate(), currentAssignment, 
            UserId.generate(), newDepartmentId, "理由");

        // When - 現在の営業部から承認
        request.approve(UserId.generate(), currentDepartmentId, "承認します");

        // Then
        assertThat(request.getStatus()).isEqualTo(AssignmentApprovalStatus.PARTIALLY_APPROVED);
        assertThat(request.getApprovals()).hasSize(1);
    }

    @Test
    @DisplayName("営業部間変更で両方の営業部が承認した場合、承認済み状態になる")
    void fullyApprovedForCrossDepartmentChange() {
        // Given
        SalesDepartmentId currentDepartmentId = SalesDepartmentId.generate();
        SalesDepartmentId newDepartmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), currentDepartmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            CustomerId.generate(), UserId.generate(), currentAssignment, 
            UserId.generate(), newDepartmentId, "理由");

        // When - 両方の営業部から承認
        request.approve(UserId.generate(), currentDepartmentId, "承認します");
        request.approve(UserId.generate(), newDepartmentId, "承認します");

        // Then
        assertThat(request.getStatus()).isEqualTo(AssignmentApprovalStatus.APPROVED);
        assertThat(request.getApprovals()).hasSize(2);
    }

    @Test
    @DisplayName("承認権限のない営業部からの承認は例外が発生する")
    void throwsExceptionWhenApprovalFromUnauthorizedDepartment() {
        // Given
        SalesDepartmentId departmentId = SalesDepartmentId.generate();
        SalesDepartmentId unauthorizedDepartmentId = SalesDepartmentId.generate();
        AssignedSalesRepresentative currentAssignment = AssignedSalesRepresentative.assignNow(
            UserId.generate(), departmentId);
        
        AssignmentChangeRequest request = AssignmentChangeRequest.create(
            CustomerId.generate(), UserId.generate(), currentAssignment, 
            UserId.generate(), departmentId, "理由");

        // When & Then
        assertThatThrownBy(() -> request.approve(UserId.generate(), unauthorizedDepartmentId, "承認します"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("この申請を承認する権限がありません");
    }
}