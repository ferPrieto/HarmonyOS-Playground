package com.fprieto.wearable.menu;

import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

import java.io.IOException;
import java.io.InputStream;

public final class ColorConverUtils {
    private static final int ADJUSTCOLOR = 0x00ffffff;
    private static final int ONE = -1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int FOUR = 4;
    private static final int NUMBER8 = 8;
    private static final int NUMBER16 = 16;
    private static final int ADJUST = 24;
    private static final int SIXTEN = 60;
    private static final int ONWSECOND = 120;
    private static final int SECONDFOUR = 240;
    private static final int ALPHAINT = 255;
    private static final int THIRDSIX = 360;

    /**
     * 构造函数
     */
    protected ColorConverUtils() {
    }

    /**
     * 透明度数值
     *
     * @param alpha alpha
     * @return alpha 透明度数值
     */
    public static int alphaValueAsInt(float alpha) {
        return Math.round(alpha * ALPHAINT);
    }

    /**
     * 透明度调整
     *
     * @param alpha alpha
     * @param color color
     * @return alpha 透明度数值
     */
    public static int adjustAlpha(float alpha, int color) {
        return alphaValueAsInt(alpha) << ADJUST | (ADJUSTCOLOR & color);
    }


    /**
     * rgb转hsv
     *
     * @param rr rr
     * @param gg gg
     * @param bb bb
     * @return rgb转hsv
     */
    public static double[] rgbToHsv(double rr, double gg, double bb) {
        double hh;
        double ss;
        double vv;
        double min;
        double max;
        min = Math.min(Math.min(rr, gg), bb);
        max = Math.max(Math.max(rr, gg), bb);
        double delta;
        vv = max;
        delta = max - min;
        if (max != 0) {
            ss = delta / max;
        } else {
            ss = 0;
            hh = ONE;
            return new double[]{hh, ss, vv};
        }

        // H
        if (rr == max) {
            hh = (gg - bb) / delta; // between yellow & magenta
        } else if (gg == max) {
            hh = SECOND + (bb - rr) / delta; // between cyan & yellow
        } else {
            hh = FOUR + (rr - gg) / delta; // between magenta & cyan
        }
        hh *= SIXTEN; // degrees
        if (hh < 0) {
            hh += THIRDSIX;
        }
        return new double[]{hh, ss, vv};
    }

    /**
     * 颜色id转rgb
     *
     * @param color color
     * @return 颜色id转rgb
     */
    public static int[] colorToRgb(int color) {
        // color id 转RGB
        int red = (color & ColorConstants.DEFAULT) >> NUMBER16;
        int green = (color & ColorConstants.DEFAULT2) >> NUMBER8;
        int blue = color & ColorConstants.DEFAULT3;
        return new int[]{red, green, blue};
    }

    /**
     * hsv格式颜色转rgb格式
     *
     * @param hsb hsb
     * @return hsv格式颜色转rgb格式
     */
    public static float[] hsb2rgb(float[] hsb) {
        float[] rgb = new float[THIRD];

        // 先令饱和度和亮度为100%，调节色相h
        for (int offset = SECONDFOUR, inum = 0; inum < THIRD; inum++, offset -= ONWSECOND) {
            // 算出色相h的值和三个区域中心点(即0°，120°和240°)相差多少，然后根据坐标图按分段函数算出rgb。
            // 但因为色环展开后，红色区域的中心点是0°同时也是360°，不好算，索性将三个区域的中心点都向右平移到240°再计算比较方便
            float diff = Math.abs(((int) hsb[0] + offset) % THIRDSIX - SECONDFOUR);

            // 如果相差小于60°则为255
            if (diff <= SIXTEN) {
                rgb[inum] = ALPHAINT;
            }

            // 如果相差在60°和120°之间，
            else if (diff > SIXTEN && diff < ONWSECOND) {
                rgb[inum] = NumCalcUtil.multiply((NumCalcUtil.subtract(1f, (NumCalcUtil.subtract(diff, SIXTEN)) / SIXTEN)), ALPHAINT);
            }

            // 如果相差大于120°则为0
            else {
                rgb[inum] = 0;
            }
        }

        // 在调节饱和度s
        for (int ii = 0; ii < THIRD; ii++) {
            rgb[ii] = NumCalcUtil.add(rgb[ii], NumCalcUtil.multiply((NumCalcUtil.subtract(ALPHAINT, rgb[ii]))
                    , (NumCalcUtil.subtract(1f, hsb[1]))));
        }

        // 最后调节亮度b
        for (int ii = 0; ii < THIRD; ii++) {
            rgb[ii] *= hsb[SECOND];
        }
        return rgb;
    }


    /**
     * 通过资源ID获取位图对象
     *
     * @param context
     * @param resId
     * @return PixelMap
     */
    public static PixelMap getPixelMap(Context context, int resId) {
        return getPixelMap(context, resId, 40, 40);
    }

    /**
     * 通过资源ID获取位图对象
     *
     * @param context
     * @param resId
     * @param width
     * @param height
     * @return PixelMap
     */
    public static PixelMap getPixelMap(Context context, int resId, int width, int height) {
        InputStream drawableInputStream = null;
        try {
            drawableInputStream = context.getResourceManager().getResource(resId);
            ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
            sourceOptions.formatHint = "image/png";
            ImageSource imageSource = ImageSource.create(drawableInputStream, null);
            ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
            decodingOptions.desiredSize = new Size(width, height);
            decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
            decodingOptions.desiredPixelFormat = PixelFormat.ARGB_8888;
            PixelMap pixelMap = imageSource.createPixelmap(decodingOptions);
            return pixelMap;
        } catch (IOException | NotExistException e) {
            e.fillInStackTrace();
        } finally {
            try {
                if (drawableInputStream != null) {
                    drawableInputStream.close();
                }
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
        return null;
    }

}