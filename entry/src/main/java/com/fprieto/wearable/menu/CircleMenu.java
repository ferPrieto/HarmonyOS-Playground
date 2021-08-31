package com.fprieto.wearable.menu;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

import java.util.ArrayList;
import java.util.List;

public class CircleMenu extends ComponentContainer implements Component.DrawTask, Component.EstimateSizeListener, Component.TouchEventListener {

    private static final int STATUS_MENU_OPEN = 1;

    private static final int STATUS_MENU_OPENED = 1 << 1;

    private static final int STATUS_MENU_CLOSE = 1 << 2;

    private static final int STATUS_MENU_CLOSE_CLEAR = 1 << 3;

    private static final int STATUS_MENU_CLOSED = 1 << 4;

    private static final int STATUS_MENU_CANCEL = 1 << 5;

    private static final int MAX_SUBMENU_NUM = 8;

    private int shadowRadius = 5;

    private int partSize;

    private int iconSize;

    private float circleMenuRadius;

    private int itemNum;

    private float itemMenuRadius;

    private float fraction, rFraction;

    private float pathLength;

    private int mainMenuColor;

    private List<Integer> subMenuColorList;

    private List<PixelMapElement> subMenuDrawableList;

    private List<RectFloat> menuRectFList;

    private int centerX, centerY;

    private int clickIndex;

    private int rotateAngle;

    private int itemIconSize;

    private int pressedColor;

    private int status;

    private boolean pressed;

    private Paint oPaint, cPaint, sPaint;

    private PathMeasure pathMeasure;

    private Path path, dstPath;

    private OnMenuSelectedListener onMenuSelectedListener;

    private OnMenuStatusChangeListener onMenuStatusChangeListener;

    public CircleMenu(Context context) {
        this(context, null);
    }

    public CircleMenu(Context context, AttrSet attrs) {
        this(context, attrs, "0");
    }

    public CircleMenu(Context context, AttrSet attrs, String defStyleAttr) {
        super(context, attrs, defStyleAttr);
        status = STATUS_MENU_OPEN;
        init();
    }

    private void init() {
        initTool();
        mainMenuColor = Color.getIntColor("#CDCDCD");
        subMenuColorList = new ArrayList<>();
        subMenuDrawableList = new ArrayList<>();
        menuRectFList = new ArrayList<>();
        addDrawTask(this);
        setEstimateSizeListener(this::onEstimateSize);
        setTouchEventListener(this::onTouchEvent);
    }

    private void initTool() {
        oPaint = new Paint();
        oPaint.setAntiAlias(true);
        oPaint.setStyle(Paint.Style.FILLANDSTROKE_STYLE);
        cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setStyle(Paint.Style.STROKE_STYLE);
        cPaint.setStrokeCap(Paint.StrokeCap.ROUND_CAP);

        sPaint = new Paint();
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.FILL_STYLE);

        path = new Path();
        dstPath = new Path();
        pathMeasure = new PathMeasure(path, false);
    }

    @Override
    public boolean onEstimateSize(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = EstimateSpec.getMode(widthMeasureSpec);
        int heightMode = EstimateSpec.getMode(heightMeasureSpec);

        int width = EstimateSpec.getSize(widthMeasureSpec);
        int height = EstimateSpec.getSize(heightMeasureSpec);
        int measureWidthSize = width, measureHeightSize = height;

        if (widthMode == EstimateSpec.NOT_EXCEED) {
            measureWidthSize = dip2px(20) * 10;
        }

        if (heightMode == EstimateSpec.NOT_EXCEED) {
            measureHeightSize = dip2px(20) * 10;
        }
        setEstimatedSize(measureWidthSize, measureHeightSize);

        if (measureWidthSize != measureHeightSize || menuRectFList.size() > 0) {
            return true;
        }
        int minSize = Math.min(getEstimatedWidth(), getEstimatedHeight());

        partSize = minSize / 10;
        iconSize = partSize * 4 / 5;
        circleMenuRadius = partSize * 3;

        centerX = getEstimatedWidth() / 2;
        centerY = getEstimatedHeight() / 2;

        path.addCircle(centerX, centerY, circleMenuRadius, Path.Direction.CLOCK_WISE);
        pathMeasure.setPath(path, true);
        pathLength = pathMeasure.getLength();

        RectFloat mainMenuRectF = new RectFloat(centerX - partSize, centerY - partSize, centerX + partSize, centerY + partSize);
        menuRectFList.add(mainMenuRectF);
        return true;
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        switch (status) {
            case STATUS_MENU_CLOSED:
                break;
            case STATUS_MENU_OPEN:
            case STATUS_MENU_CANCEL:
            case STATUS_MENU_OPENED:
                drawSubMenu(canvas);
                break;
            case STATUS_MENU_CLOSE:
                drawSubMenu(canvas);
                drawCircleMenu(canvas);
                break;
            case STATUS_MENU_CLOSE_CLEAR:
                drawCircleMenu(canvas);
                break;
        }
    }

    private void drawCircleMenu(Canvas canvas) {
        if (status == STATUS_MENU_CLOSE) {
            drawCirclePath(canvas);
            drawCircleIcon(canvas);
        } else {
            cPaint.setStrokeWidth(partSize * 2 + partSize * .5f * fraction);
            cPaint.setColor(calcAlphaColor(getIntClickMenuColor(), true));
            canvas.drawCircle(centerX, centerY, circleMenuRadius + partSize * .5f * fraction, cPaint);
        }
    }

    private int getIntClickMenuColor() {
        return clickIndex == 0 ? mainMenuColor : subMenuColorList.get(clickIndex - 1);
    }

    private Color getClickMenuColor() {
        return new Color(clickIndex == 0 ? mainMenuColor : subMenuColorList.get(clickIndex - 1));
    }

    private void drawCircleIcon(Canvas canvas) {
        canvas.save();
        PixelMapElement selDrawable = subMenuDrawableList.get(clickIndex - 1);
        if (selDrawable == null) return;
        int startAngle = (clickIndex - 1) * (360 / itemNum);
        int endAngle = 360 + startAngle;
        int itemX = (int) (centerX + Math.sin(Math.toRadians((endAngle - startAngle) * fraction + startAngle)) * circleMenuRadius);
        int itemY = (int) (centerY - Math.cos(Math.toRadians((endAngle - startAngle) * fraction + startAngle)) * circleMenuRadius);
        canvas.rotate(360 * fraction, itemX, itemY);
        selDrawable.setBounds(itemX - iconSize / 2, itemY - iconSize / 2, itemX + iconSize / 2, itemY + iconSize / 2);
        selDrawable.drawToCanvas(canvas);
        canvas.restore();
    }

    private void drawCirclePath(Canvas canvas) {
        canvas.save();
        canvas.rotate(rotateAngle, centerX, centerY);
        dstPath.reset();
        dstPath.lineTo(0, 0);
        pathMeasure.getSegment(0, pathLength * fraction, dstPath, true);
        cPaint.setStrokeWidth(partSize * 2);
        cPaint.setColor(getClickMenuColor());
        canvas.drawPath(dstPath, cPaint);
        canvas.restore();
    }

    private void drawSubMenu(Canvas canvas) {
        int itemX, itemY, angle;
        final float offsetRadius = 1.5f;
        RectFloat menuRectF;
        for (int i = 0; i < itemNum; i++) {
            angle = i * (360 / itemNum);
            if (status == STATUS_MENU_OPEN) {
                itemX = (int) (centerX + Math.sin(Math.toRadians(angle)) * (circleMenuRadius - (1 - fraction) * partSize * offsetRadius));
                itemY = (int) (centerY - Math.cos(Math.toRadians(angle)) * (circleMenuRadius - (1 - fraction) * partSize * offsetRadius));
                oPaint.setColor(calcAlphaColor(subMenuColorList.get(i), false));
                sPaint.setColor(calcAlphaColor(subMenuColorList.get(i), false));
            } else if (status == STATUS_MENU_CANCEL) {
                itemX = (int) (centerX + Math.sin(Math.toRadians(angle)) * (circleMenuRadius - fraction * partSize * offsetRadius));
                itemY = (int) (centerY - Math.cos(Math.toRadians(angle)) * (circleMenuRadius - fraction * partSize * offsetRadius));
                oPaint.setColor(calcAlphaColor(subMenuColorList.get(i), true));
                sPaint.setColor(calcAlphaColor(subMenuColorList.get(i), true));
            } else {
                itemX = (int) (centerX + Math.sin(Math.toRadians(angle)) * circleMenuRadius);
                itemY = (int) (centerY - Math.cos(Math.toRadians(angle)) * circleMenuRadius);
                oPaint.setColor(new Color(subMenuColorList.get(i)));
                sPaint.setColor(new Color(subMenuColorList.get(i)));
            }
            if (pressed && clickIndex - 1 == i) {
                oPaint.setColor(new Color(pressedColor));
            }
            drawMenuShadow(canvas, itemX, itemY, itemMenuRadius);
            canvas.drawCircle(itemX, itemY, itemMenuRadius, oPaint);
            drawSubMenuIcon(canvas, itemX, itemY, i);
            menuRectF = new RectFloat(itemX - partSize, itemY - partSize, itemX + partSize, itemY + partSize);
            if (menuRectFList.size() - 1 > i) {
                menuRectFList.remove(i + 1);
            }
            menuRectFList.add(i + 1, menuRectF);
        }
    }

    private void drawSubMenuIcon(Canvas canvas, int centerX, int centerY, int index) {
        int diff;
        if (status == STATUS_MENU_OPEN || status == STATUS_MENU_CANCEL) {
            diff = itemIconSize / 2;
        } else {
            diff = iconSize / 2;
        }
        resetBoundsAndDrawIcon(canvas, subMenuDrawableList.get(index), centerX, centerY, diff);
    }

    private void resetBoundsAndDrawIcon(Canvas canvas, PixelMapElement drawable, int centerX, int centerY, int diff) {
        if (drawable == null) return;
        drawable.setBounds(centerX - diff, centerY - diff, centerX + diff, centerY + diff);
        drawable.drawToCanvas(canvas);
    }

    private void drawMenuShadow(Canvas canvas, int centerX, int centerY, float radius) {
        if (radius + shadowRadius > 0) {
            Color[] colors = new Color[]{Color.BLACK, Color.TRANSPARENT};
            sPaint.setShader(new RadialShader(new Point((float) centerX, (float) centerY), radius + shadowRadius,
                    null, colors, Shader.TileMode.CLAMP_TILEMODE), Paint.ShaderType.RADIAL_SHADER);
            canvas.drawCircle(centerX, centerY, radius + shadowRadius, sPaint);
        }
    }

    @Override
    public boolean onTouchEvent(Component component, TouchEvent event) {
        if (status == STATUS_MENU_CLOSE || status == STATUS_MENU_CLOSE_CLEAR) return true;
        MmiPoint point = event.getPointerPosition(event.getIndex());

        int index = clickWhichRectF(point.getX(), point.getY());
        switch (event.getAction()) {
            case TouchEvent.PRIMARY_POINT_DOWN:
                pressed = true;
                if (index != -1) {
                    clickIndex = index;
                    updatePressEffect(index, true);
                }
                break;
            case TouchEvent.OTHER_POINT_DOWN:
            case TouchEvent.OTHER_POINT_UP:
                break;
            case TouchEvent.POINT_MOVE:
                if (index == -1) {
                    pressed = false;
                    invalidate();
                }
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                pressed = false;
                if (index != -1) {
                    clickIndex = index;
                    updatePressEffect(index, false);
                }
                if (index == 0) {
                    status = STATUS_MENU_OPEN;
                    if (status == STATUS_MENU_OPENED) {
                        status = STATUS_MENU_CANCEL;
                        startCancelMenuAnima();
                    }
                    startOpenMenuAnima();
                } else {
                    if (status == STATUS_MENU_OPENED && index != -1) {
                        status = STATUS_MENU_CLOSE;
                        rotateAngle = clickIndex * (360 / itemNum) - (360 / itemNum) - 90;
                        startCloseMenuAnimation(index - 1);
                    }
                }
                break;
        }
        return true;
    }

    private void updatePressEffect(int menuIndex, boolean press) {
        if (press) {
            pressedColor = calcPressedEffectColor(menuIndex, .15f);
        }
        invalidate();
    }

    private int calcPressedEffectColor(int menuIndex, float depth) {
        int color = menuIndex == 0 ? mainMenuColor : subMenuColorList.get(menuIndex - 1);
        float[] hsv = new float[3];
        int red = (color & ColorConstants.DEFAULT) >> ColorConstants.ONESIX;
        int green = (color & ColorConstants.DEFAULT2) >> ColorConstants.EAT;
        int blue = color & ColorConstants.DEFAULT3;
        ColorConverUtils.rgbToHsv(red, green, blue);
        hsv[2] *= (1.f - depth);
        return Color.GRAY.getValue();
    }

    private Color calcAlphaColor(int color, boolean reverse) {
        int alpha;
        if (reverse) {
            alpha = (int) (255 * (1.f - fraction));
        } else {
            alpha = (int) (255 * fraction);
        }
        if (alpha >= 255) alpha = 255;
        if (alpha <= 0) alpha = 0;
        if (alpha >= 0 && alpha <= 255) {
            return new Color(color & 16777215 | alpha << 24);
        } else {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
    }

    public void startOpenMenuAnima() {
        AnimatorValue openAnima = new AnimatorValue();
        openAnima.setDuration(500);
        openAnima.setCurveType(Animator.CurveType.OVERSHOOT);
        openAnima.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                status = STATUS_MENU_OPENED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuOpened();
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        openAnima.setValueUpdateListener((animatorValue, interpolatedTime) -> {
            fraction = interpolatedTime;
            itemMenuRadius = fraction * partSize;
            itemIconSize = (int) (fraction * iconSize);
            invalidate();
        });
        openAnima.start();
    }

    private void startCancelMenuAnima() {
        AnimatorValue cancelAnima = new AnimatorValue();
        cancelAnima.setDuration(500);
        cancelAnima.setCurveType(Animator.CurveType.ANTICIPATE);
        cancelAnima.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                status = STATUS_MENU_CLOSED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuClosed();
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        cancelAnima.setValueUpdateListener((animatorValue, interpolatedTime) -> {
            fraction = interpolatedTime;
            itemMenuRadius = (1 - fraction) * partSize;
            itemIconSize = (int) ((1 - fraction) * iconSize);
            invalidate();
        });
        cancelAnima.start();
    }

    private void startCloseMenuAnimation(int itemSelected) {
        AnimatorValue rotateAnima = new AnimatorValue();
        AnimatorValue aroundAnima = new AnimatorValue();
        aroundAnima.setDuration(600);
        aroundAnima.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE);
        aroundAnima.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                status = STATUS_MENU_CLOSE_CLEAR;
                rotateAnima.start();
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        aroundAnima.setValueUpdateListener((animatorValue, interpolatedTime) -> {
            fraction = interpolatedTime;
            float animaFraction = fraction * 2 >= 1 ? 1 : fraction * 2;
            itemIconSize = (int) ((1 - animaFraction) * iconSize);
            invalidate();
        });
        aroundAnima.start();
        AnimatorValue spreadAnima = new AnimatorValue();
        spreadAnima.setDuration(600);
        spreadAnima.setCurveType(Animator.CurveType.LINEAR);
        spreadAnima.setValueUpdateListener((animatorValue, interpolatedTime) -> fraction = interpolatedTime);
        spreadAnima.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                status = STATUS_MENU_CLOSED;
                if (onMenuStatusChangeListener != null)
                    onMenuStatusChangeListener.onMenuClosed();
                invalidate();
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });

        rotateAnima.setCurveType(Animator.CurveType.OVERSHOOT);
        rotateAnima.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
                spreadAnima.start();
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                if (onMenuSelectedListener != null)
                    onMenuSelectedListener.onMenuSelected(itemSelected);
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        rotateAnima.setValueUpdateListener((animatorValue, interpolatedTime) -> {
            fraction = interpolatedTime;
            itemIconSize = (int) (rFraction * iconSize);
            invalidate();
        });
    }

    private int clickWhichRectF(float x, float y) {
        int which = -1;
        for (RectFloat rectF : menuRectFList) {
            if (rectF.isInclude(x, y)) {
                which = menuRectFList.indexOf(rectF);
                break;
            }
        }
        return which;
    }

    private PixelMapElement convertDrawable(int iconRes) {
        PixelMapElement pixelMapElement = new PixelMapElement(ColorConverUtils.getPixelMap(getContext(), iconRes));
        return pixelMapElement;
    }

    public CircleMenu addSubMenu(int menuColor, int menuRes) {
        if (subMenuColorList.size() < MAX_SUBMENU_NUM && subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuColorList.add(menuColor);
            subMenuDrawableList.add(convertDrawable(menuRes));
            itemNum = Math.min(subMenuColorList.size(), subMenuDrawableList.size());
        }
        return this;
    }

    public CircleMenu setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    public CircleMenu setOnMenuStatusChangeListener(OnMenuStatusChangeListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        final float scale = DisplayManager.getInstance().getDefaultDisplay(getContext()).get().getRealAttributes().densityPixels;
        return (int) (dpValue * scale + 0.5f);
    }
}