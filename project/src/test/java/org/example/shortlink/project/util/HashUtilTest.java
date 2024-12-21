package org.example.shortlink.project.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



class HashUtilTest {

    @Test
    void hashToBase62() {
        Assertions.assertSame("abcdef", HashUtil.hashToBase62("https://blog.csdn.net/qq_46311811/article/details/122517976"));
    }
}