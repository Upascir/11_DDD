package com.customermaster.domain.user;

import com.customermaster.domain.shared.Entity;
import com.customermaster.domain.salesdepartment.SalesDepartmentId;
import com.customermaster.domain.customer.CustomerId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ユーザー
 * 
 * システムを利用するユーザーを表現するエンティティ（集約ルート）
 */
public class User extends Entity<UserId> {
    
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;
    
    private UserName userName;
    private Role role;
    private SalesDepartmentId departmentId;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    
    /**
     * デフォルトコンストラクタ（使用禁止）
     */
    private User() {
        super(null);
        throw new UnsupportedOperationException("ファクトリメソッドを使用してください");
    }
    
    /**
     * コンストラクタ（新規ユーザー用）
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param role 役割
     * @param departmentId 所属部門ID
     */
    private User(UserId userId, UserName userName, Role role, SalesDepartmentId departmentId) {
        super(userId);
        this.userName = Objects.requireNonNull(userName, "ユーザー名は必須です");
        this.role = Objects.requireNonNull(role, "役割は必須です");
        this.departmentId = validateDepartmentAssignment(role, departmentId);
        this.status = UserStatus.ACTIVE;
        this.lastLoginAt = null;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * コンストラクタ（既存ユーザー復元用）
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param role 役割
     * @param departmentId 所属部門ID
     * @param status ユーザー状態
     * @param lastLoginAt 最終ログイン日時
     * @param failedLoginAttempts ログイン失敗回数
     * @param lockedUntil ロック解除日時
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    private User(UserId userId, UserName userName, Role role, SalesDepartmentId departmentId,
                UserStatus status, LocalDateTime lastLoginAt, int failedLoginAttempts,
                LocalDateTime lockedUntil, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(userId, createdAt, updatedAt);
        this.userName = Objects.requireNonNull(userName, "ユーザー名は必須です");
        this.role = Objects.requireNonNull(role, "役割は必須です");
        this.departmentId = validateDepartmentAssignment(role, departmentId);
        this.status = Objects.requireNonNull(status, "ユーザー状態は必須です");
        this.lastLoginAt = lastLoginAt;
        this.failedLoginAttempts = Math.max(0, failedLoginAttempts);
        this.lockedUntil = lockedUntil;
    }
    
    /**
     * 新規ユーザーを作成
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param role 役割
     * @param departmentId 所属部門ID
     * @return User
     */
    public static User create(UserId userId, UserName userName, Role role, SalesDepartmentId departmentId) {
        return new User(userId, userName, role, departmentId);
    }
    
    /**
     * 既存ユーザーを復元
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param role 役割
     * @param departmentId 所属部門ID
     * @param status ユーザー状態
     * @param lastLoginAt 最終ログイン日時
     * @param failedLoginAttempts ログイン失敗回数
     * @param lockedUntil ロック解除日時
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return User
     */
    public static User restore(UserId userId, UserName userName, Role role, SalesDepartmentId departmentId,
                              UserStatus status, LocalDateTime lastLoginAt, int failedLoginAttempts,
                              LocalDateTime lockedUntil, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(userId, userName, role, departmentId, status, lastLoginAt,
                       failedLoginAttempts, lockedUntil, createdAt, updatedAt);
    }
    
    /**
     * 部門割り当ての妥当性を検証
     */
    private SalesDepartmentId validateDepartmentAssignment(Role role, SalesDepartmentId departmentId) {
        if (role.isSalesRepresentative()) {
            if (departmentId == null) {
                throw new IllegalArgumentException("営業担当者・部長は営業部への所属が必須です");
            }
        } else if (role.isSystemAdministrator()) {
            // システム管理者は情報システム部所属（departmentIdはnullでも可）
        }
        return departmentId;
    }
    
    // ビジネスメソッド
    
    /**
     * 法人データ変更を承認できるかどうかを判定
     * 
     * 注意: 部門一致確認は呼び出し側またはドメインサービスで行う必要がある
     * 
     * @param customerId 法人ID
     * @return 基本的な承認権限を持つ場合true
     */
    public boolean canApproveCustomerChange(CustomerId customerId) {
        if (!canUseSystem()) {
            return false;
        }
        
        // システム管理者は全ての変更を承認可能
        if (role.isSystemAdministrator()) {
            return true;
        }
        
        // 部長は基本的な承認権限を持つ（部門一致確認は別途必要）
        return role.isDepartmentManager();
    }
    
    /**
     * 担当者変更を承認できるかどうかを判定
     * 
     * @return 承認可能な場合true
     */
    public boolean canApproveAssignmentChange() {
        if (!canUseSystem()) {
            return false;
        }
        
        return role.canApprove();
    }
    
    /**
     * 法人データを直接更新できるかどうかを判定
     * 
     * @param customerId 法人ID
     * @return 直接更新可能な場合true
     */
    public boolean canDirectlyUpdateCustomer(CustomerId customerId) {
        if (!canUseSystem()) {
            return false;
        }
        
        // システム管理者は直接更新可能
        return role.isSystemAdministrator();
    }
    
    /**
     * ログイン失敗を記録
     */
    public void recordLoginFailure() {
        this.failedLoginAttempts++;
        
        if (this.failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
        }
        
        markAsUpdated();
    }
    
    /**
     * ログイン成功を記録
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        
        // ロック状態の場合はアクティブに戻す
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
        
        markAsUpdated();
    }
    
    /**
     * アカウントがロックされているかどうかを判定
     * 
     * @return ロックされている場合true
     */
    public boolean isAccountLocked() {
        if (status != UserStatus.LOCKED) {
            return false;
        }
        
        // ロック期間が過ぎている場合は自動解除
        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            this.status = UserStatus.ACTIVE;
            this.lockedUntil = null;
            this.failedLoginAttempts = 0;
            markAsUpdated();
            return false;
        }
        
        return true;
    }
    
    /**
     * システムを利用できるかどうかを判定
     * 
     * @return 利用可能な場合true
     */
    public boolean canUseSystem() {
        return status.canUseSystem() && !isAccountLocked();
    }
    
    /**
     * ユーザーを無効化
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        markAsUpdated();
    }
    
    /**
     * ユーザーを有効化
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        markAsUpdated();
    }
    
    /**
     * ユーザーを削除（論理削除）
     */
    public void delete() {
        this.status = UserStatus.DELETED;
        markAsUpdated();
    }
    
    /**
     * 役割を変更
     * 
     * @param newRole 新しい役割
     * @param newDepartmentId 新しい所属部門ID
     */
    public void changeRole(Role newRole, SalesDepartmentId newDepartmentId) {
        this.role = Objects.requireNonNull(newRole, "役割は必須です");
        this.departmentId = validateDepartmentAssignment(newRole, newDepartmentId);
        markAsUpdated();
    }
    
    // Getters
    
    public UserName getUserName() { return userName; }
    public Role getRole() { return role; }
    public SalesDepartmentId getDepartmentId() { return departmentId; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    
    @Override
    public String toString() {
        return "User{" +
               "id=" + getId() +
               ", userName=" + userName +
               ", role=" + role +
               ", departmentId=" + departmentId +
               ", status=" + status +
               ", lastLoginAt=" + lastLoginAt +
               ", failedLoginAttempts=" + failedLoginAttempts +
               ", lockedUntil=" + lockedUntil +
               '}';
    }
}