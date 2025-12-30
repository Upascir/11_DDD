package com.customermaster;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * アプリケーション起動テスト
 */
@SpringBootTest
@ActiveProfiles("test")
class CustomerMasterSystemApplicationTests {

    @Test
    void contextLoads() {
        // Spring Bootアプリケーションが正常に起動することを確認
    }
}