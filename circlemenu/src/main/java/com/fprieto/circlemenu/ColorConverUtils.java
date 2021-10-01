package com.fprieto.circlemenu;

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
    private static final int ONE = -1;
    private static final int SECOND = 2;
    private static final int FOUR = 4;
    private static final int SIXTEN = 60;
    private static final int THIRDSIX = 360;

    protected ColorConverUtils() {
    }

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

    public static PixelMap getPixelMap(Context context, int resId) {
        return getPixelMap(context, resId, 40, 40);
    }

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