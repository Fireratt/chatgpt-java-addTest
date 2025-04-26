package com.plexpt.chatgpt;

import com.plexpt.chatgpt.util.FormatDateUtil;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class FormatDateUtilTest {

    @Test
    void testFormatDate() throws ParseException {
        // 准备测试数据
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = inputFormat.parse("2025-04-26");

        // 调用FormatDateUtil
        String formattedDate = FormatDateUtil.formatDate(date);

        // 验证输出
        assertEquals("2025-04-26", formattedDate, "Formatted date should match expected pattern yyyy-MM-dd");
    }

    @Test
    void testFormatDateWithNull() {
        // 如果传入null，应该抛出NullPointerException
        assertThrows(NullPointerException.class, () -> {
            FormatDateUtil.formatDate(null);
        }, "Passing null should throw NullPointerException");
    }
}

