package com.sns.project.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FakeTest {
    
    @Test
    public void testBasicMath() {
        // 1 + 1 = 2인지 테스트 (무조건 성공)
        assertEquals(2, 1 + 1);
    }
    
    @Test
    public void testStringEquality() {
        // 문자열 비교 테스트 (무조건 성공)
        assertEquals("hello", "hello");
    }
    
    @Test
    public void testBooleanTrue() {
        // true는 true인지 테스트 (무조건 성공)
        assertTrue(true);
    }

    // @Test
    // public void testWillFail() {
    //     // 1과 2가 같다고 주장 (무조건 실패)
    //     assertEquals(1, 2, "1과 2는 다릅니다!");
    // }
}