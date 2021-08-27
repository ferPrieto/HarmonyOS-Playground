package com.fprieto.wearable.menu;

import java.math.BigDecimal;

public class NumCalcUtil {
    private NumCalcUtil() {
    }
    /**
     * 加法
     *
     * @param num1 num1
     * @param num2 num2
     * @return 结果 结果
     */
    public static float add(float num1, float num2) {
        return new BigDecimal(num1).add(new BigDecimal(num2)).floatValue();
    }

    /**
     * 减法
     *
     * @param num1 num1
     * @param num2 num2
     * @return 结果 结果
     */
    public static float subtract(float num1, float num2) {
        return new BigDecimal(num1).subtract(new BigDecimal(num2)).floatValue();
    }

    /**
     * 除法
     *
     * @param num1 num1
     * @param num2 num2
     * @return 结果 结果
     */
    public static float divide(float num1, float num2) {
        return new BigDecimal(num1).divide(new BigDecimal(num2)).floatValue();
    }

    /**
     * 乘法
     *
     * @param num1 num1
     * @param num2 num2
     * @return 结果 结果
     */
    public static float multiply(float num1, float num2) {
        return new BigDecimal(num1).multiply(new BigDecimal(num2)).floatValue();
    }
}
