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
import ohos.media.image.PixelMap;
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

    private  int shadowRadius = 5;

    private int partSize;

    private int iconSize;

    private float circleMenuRadius;

    private int itemNum;

    private float itemMenuRadius;

    private float fraction, rFraction;

    private float pathLength;

    private int mainMenuColor;

    private PixelMapElement openMenuIcon, closeMenuIcon;

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

    /**
     * 构造方法
     *
     * @param context context
     */
    public CircleMenu(Context context) {
        this(context, null);
    }

    /**
     * CircleMenu
     *
     * @param context context
     * @param attrs   attrs
     */
    public CircleMenu(Context context, AttrSet attrs) {
        this(context, attrs, "0");
    }

    /**
     * 构造方法
     *
     * @param context      context
     * @param attrs        attrs
     * @param defStyleAttr defStyleAttr
     */
    public CircleMenu(Context context, AttrSet attrs, String defStyleAttr) {
        super(context, attrs, defStyleAttr);
        status = STATUS_MENU_CLOSED;
        init();
    }

    private void init() {
        initTool();
        mainMenuColor = Color.getIntColor("#CDCDCD");
//        openMenuIcon = new PixelMapElement();
//        closeMenuIcon = new PixelMapElement();
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
        resetMainDrawableBounds();

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
                drawMainMenu(canvas);
                break;
            case STATUS_MENU_OPEN:
                drawMainMenu(canvas);
                drawSubMenu(canvas);
                break;
            case STATUS_MENU_OPENED:
                drawMainMenu(canvas);
                drawSubMenu(canvas);
                break;
            case STATUS_MENU_CLOSE:
                drawMainMenu(canvas);
                drawSubMenu(canvas);
                drawCircleMenu(canvas);
                break;
            case STATUS_MENU_CLOSE_CLEAR:
                drawMainMenu(canvas);
                drawCircleMenu(canvas);
                break;
            case STATUS_MENU_CANCEL:
                drawMainMenu(canvas);
                drawSubMenu(canvas);
                break;
        }
    }

    /**
     * 绘制周围子菜单环绕的圆环路径
     *
     * @param canvas canvas
     */
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

    /**
     * 绘制子菜单转动时的图标
     *
     * @param canvas canvas
     */
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

    /**
     * 绘制子菜单项转动时的轨迹路径
     *
     * @param canvas canvas
     */
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

    /**
     * 绘制周围子菜单项按钮
     *
     * @param canvas canvas
     */
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

    /**
     * 绘制子菜单项图标
     *
     * @param canvas canvas
     * @param centerX centerX
     * @param centerY centerY
     * @param index index
     */
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

    /**
     * 绘制中间的菜单开关按钮
     *
     * @param canvas canvas
     */
    private void drawMainMenu(Canvas canvas) {
        float centerMenuRadius, realFraction;
        if (status == STATUS_MENU_CLOSE) {
            // 中心主菜单按钮以两倍速度缩小
            realFraction = (1 - fraction * 2) == 0 ? 0 : (1 - fraction * 2);
            centerMenuRadius = partSize * realFraction;
        } else if (status == STATUS_MENU_CLOSE_CLEAR) {
            // 中心主菜单按钮以四倍速度扩大
            realFraction = fraction * 4 >= 1 ? 1 : fraction * 4;
            centerMenuRadius = partSize * realFraction;
        } else {
            centerMenuRadius = partSize;
        }
        if (status == STATUS_MENU_OPEN || status == STATUS_MENU_OPENED || status == STATUS_MENU_CLOSE) {
            oPaint.setColor(new Color(calcPressedEffectColor(0, .5f)));
        } else if (pressed && clickIndex == 0) {
            oPaint.setColor(new Color(pressedColor));
        } else {
            oPaint.setColor(new Color(mainMenuColor));
            sPaint.setColor(new Color(mainMenuColor));
        }
        drawMenuShadow(canvas, centerX, centerY, centerMenuRadius);
        canvas.drawCircle(centerX, centerY, centerMenuRadius, oPaint);
        drawMainMenuIcon(canvas);
    }

    private void drawMainMenuIcon(Canvas canvas) {
        canvas.save();
        switch (status) {
            case STATUS_MENU_CLOSED:
                if (openMenuIcon != null)
                    canvas.drawPixelMapHolder(new PixelMapHolder(openMenuIcon.getPixelMap()), centerX - iconSize / 2, centerY - iconSize / 2, oPaint);

                break;
            case STATUS_MENU_OPEN:
                canvas.rotate(45 * (fraction - 1), centerX, centerY);
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, centerX, centerY, iconSize / 2);
                break;
            case STATUS_MENU_OPENED:
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, centerX, centerY, iconSize / 2);
                break;
            case STATUS_MENU_CLOSE:
                resetBoundsAndDrawIcon(canvas, closeMenuIcon, centerX, centerY, itemIconSize / 2);
                break;
            case STATUS_MENU_CLOSE_CLEAR:
                canvas.rotate(90 * (rFraction - 1), centerX, centerY);
                resetBoundsAndDrawIcon(canvas, openMenuIcon, centerX, centerY, itemIconSize / 2);
                break;
            case STATUS_MENU_CANCEL:
                canvas.rotate(-45 * fraction, centerX, centerY);
                if (closeMenuIcon != null)

                    closeMenuIcon.drawToCanvas(canvas);
                break;
        }
        canvas.restore();
    }

    /**
     * 绘制菜单按钮阴影
     *
     * @param canvas canvas
     * @param centerX centerX
     * @param centerY centerY
     * @param radius radius
     */
    private void drawMenuShadow(Canvas canvas, int centerX, int centerY, float radius) {
        if (radius + shadowRadius > 0) {
//            sPaint.setShader(new RadialShader(centerX, centerY, radius + shadowRadius,
//                    Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP_TILEMODE));
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
                    updatePressEffect(index, pressed);
                }
                break;
            case TouchEvent.OTHER_POINT_DOWN:
                break;
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
                    updatePressEffect(index, pressed);
                }
                if (index == 0) { // 点击的是中间的按钮
                    if (status == STATUS_MENU_CLOSED) {
                        status = STATUS_MENU_OPEN;
                        startOpenMenuAnima();
                    } else if (status == STATUS_MENU_OPENED) {
                        status = STATUS_MENU_CANCEL;
                        startCancelMenuAnima();
                    }
                } else { // 点击的是周围子菜单项按钮
                    if (status == STATUS_MENU_OPENED && index != -1) {
                        status = STATUS_MENU_CLOSE;
                        if (onMenuSelectedListener != null)
                            onMenuSelectedListener.onMenuSelected(index - 1);
                        rotateAngle = clickIndex * (360 / itemNum) - (360 / itemNum) - 90;
                        startCloseMeunAnima();
                    }
                }
                break;
        }
        return true;
    }


    /**
     * 更新按钮的状态
     *
     * @param menuIndex menuIndex
     * @param press press
     */
    private void updatePressEffect(int menuIndex, boolean press) {
        if (press) {
            pressedColor = calcPressedEffectColor(menuIndex, .15f);
        }
        invalidate();
    }

    /**
     * 获取按钮被按下的颜色
     *
     * @param menuIndex menuIndex
     * @param depth 取值范围为[0, 1].值越大，颜色越深
     * @return Color.GRAY.getValue() Color.GRAY.getValue()
     */
    private int calcPressedEffectColor(int menuIndex, float depth) {
        int color = menuIndex == 0 ? mainMenuColor : subMenuColorList.get(menuIndex - 1);
        float[] hsv = new float[3];
        // float[] hsvInfo = new float[Constant.THIRD];
        // color id 转RGB
        int red = (color & ColorConstants.DEFAULT) >> ColorConstants.ONESIX;
        int green = (color & ColorConstants.DEFAULT2) >> ColorConstants.EAT;
        int blue = color & ColorConstants.DEFAULT3;
//        double[] hsvColor = ColorConverUtils.rgbToHsv(red, green, blue);
        ColorConverUtils.rgbToHsv(red, green, blue);
//        Color.colorToHSV(color, hsv);
        hsv[2] *= (1.f - depth);
        // float[] rgb = ColorConverUtils.hsb2rgb(hsvInfo);
        //    int hsvColor = Color.rgb((int) rgb[0], (int) rgb[1], (int) rgb[SECOND]);

        return Color.GRAY.getValue();
    }


    /**
     * 用于完成在 View 中的圆环逐渐扩散消失的动画效果 <br/>
     * <p>
     * 根据 fraction 调整 color 的 Alpha 值
     *
     * @param color   被调整 Alpha 值的颜色
     * @param reverse true : 由不透明到透明的顺序调整，否则就逆序
     * @return Color Color
     */
    private Color calcAlphaColor(int color, boolean reverse) {
        int alpha;
        if (reverse) { // 由不透明到透明
            alpha = (int) (255 * (1.f - fraction));
        } else { // 由透明到不透明
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

    /**
     * 启动打开菜单动画
     */
    private void startOpenMenuAnima() {
//        ofFloat(1.f, 100.f)
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
        openAnima.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float interpolatedTime) {
                fraction = interpolatedTime;
                itemMenuRadius = fraction * partSize;
                itemIconSize = (int) (fraction * iconSize);
                invalidate();
            }
        });
        openAnima.start();
    }

    /**
     * 启动取消动画
     */
    private void startCancelMenuAnima() {
        //        ofFloat(1.f, 100.f)
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
        cancelAnima.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float interpolatedTime) {
                fraction = interpolatedTime;
                itemMenuRadius = (1 - fraction) * partSize;
                itemIconSize = (int) ((1 - fraction) * iconSize);
                invalidate();
            }
        });
        cancelAnima.start();
    }

    /**
     * 开启关闭菜单动画 </br>
     * <p>关闭菜单动画分为三部分</p>
     * <ur>
     * <li>选中菜单项转动一周</li>
     * <li>环状轨迹扩散消失</li>
     * <li>主菜单按钮旋转</li>
     * </ur>
     */
    private void startCloseMeunAnima() {
        // 选中菜单项转动一周动画驱动
        //        ofFloat(1.f, 100.f)
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
        aroundAnima.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float interpolatedTime) {
                fraction = interpolatedTime;
                // 中心主菜单图标以两倍速度缩小
                float animaFraction = fraction * 2 >= 1 ? 1 : fraction * 2;
                itemIconSize = (int) ((1 - animaFraction) * iconSize);
                invalidate();
            }
        });
        aroundAnima.start();
        // 环状轨迹扩散消失动画驱动
        //        ofFloat(1.f, 100.f)
        AnimatorValue spreadAnima = new AnimatorValue();
        spreadAnima.setDuration(600);
        spreadAnima.setCurveType(Animator.CurveType.LINEAR);
        spreadAnima.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float interpolatedTime) {
                fraction = interpolatedTime;
            }
        });
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

        // 主菜单转动动画驱动
        //        ofFloat(1.f, 100.f)
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

            }

            @Override
            public void onPause(Animator animator) {

            }

            @Override
            public void onResume(Animator animator) {

            }
        });
        rotateAnima.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float interpolatedTime) {
                fraction = interpolatedTime;
                itemIconSize = (int) (rFraction * iconSize);
                invalidate();
            }
        });

//        AnimatorGroup closeAnimaSet = new AnimatorGroup();
//        closeAnimaSet.setDuration(500);
//        AnimatorGroup.Builder animatorGroupBuilder = closeAnimaSet.build();
//        // 4个动画的顺序为: am1 -> am2/am3 -> am4
//        animatorGroupBuilder.addAnimators(spreadAnima, rotateAnima);
//        closeAnimaSet.setStateChangedListener(new Animator.StateChangedListener() {
//            @Override
//            public void onStart(Animator animator) {
//              System.out.println("zzmzzm-"+"closeanimal");
//            }
//
//            @Override
//            public void onStop(Animator animator) {
//                System.out.println("guanbidonghua-->"+"onStoponStop");
//
//            }
//
//            @Override
//            public void onCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onEnd(Animator animator) {
//                status = STATUS_MENU_CLOSED;
//                System.out.println("zzmzzm-onEnd"+"closeanimalstatus+=="+status);
//
//                if (onMenuStatusChangeListener != null)
//                    onMenuStatusChangeListener.onMenuClosed();
//                invalidate();
//                closeAnimaSet.clear();
//                closeAnimaSet.release();
//            }
//
//            @Override
//            public void onPause(Animator animator) {
//
//            }
//
//            @Override
//            public void onResume(Animator animator) {
//
//            }
//        });
//        AnimatorSet closeAnimaSet = new AnimatorSet();
//        closeAnimaSet.setDuration(500);
//        closeAnimaSet.play(spreadAnima).with(rotateAnima);
//        closeAnimaSet.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                status = STATUS_MENU_CLOSED;
//                if (onMenuStatusChangeListener != null)
//                    onMenuStatusChangeListener.onMenuClosed();
//            }
//        });
//        AnimatorGroup animatorSet = new AnimatorGroup();
//        animatorSet.setDuration(500);
//        AnimatorGroup.Builder animatorSetGroupBuilder = animatorSet.build();
//        // 4个动画的顺序为: am1 -> am2/am3 -> am4
//        animatorSetGroupBuilder.addAnimators(aroundAnima).addAnimators(closeAnimaSet);
//        animatorSet.setStateChangedListener(new Animator.StateChangedListener() {
//            @Override
//            public void onStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onStop(Animator animator) {
//
//            }
//
//            @Override
//            public void onCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onEnd(Animator animator) {
//                animatorSet.clear();
//                animatorSet.release();
//            }
//
//            @Override
//            public void onPause(Animator animator) {
//
//            }
//
//            @Override
//            public void onResume(Animator animator) {
//
//            }
//        });
//        animatorSet.start();

    }

    /**
     * 获取当前点击的是哪一个菜单按钮 <br/>
     * 中心菜单下标为0，周围菜单从正上方顺时针计数1~5
     *
     * @param x x
     * @param y y
     * @return int int
     */
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

    private PixelMapElement convertBitmap(PixelMap bitmap) {
        return new PixelMapElement(bitmap);
    }

    private void resetMainDrawableBounds() {
        openMenuIcon.setBounds(centerX - iconSize / 2, centerY - iconSize / 2,
                centerX + iconSize / 2, centerY + iconSize / 2);
        closeMenuIcon.setBounds(centerX - iconSize / 2, centerY - iconSize / 2,
                centerX + iconSize / 2, centerY + iconSize / 2);
    }

    /**
     * 设置主菜单的背景色，以及打开/关闭的图标
     *
     * @param mainMenuColor 主菜单背景色
     * @param openMenuRes   菜单打开图标，Resource 格式
     * @param closeMenuRes  菜单关闭图标，Resource 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu setMainMenu(int mainMenuColor, int openMenuRes, int closeMenuRes) {
        openMenuIcon = convertDrawable(openMenuRes);
        closeMenuIcon = convertDrawable(closeMenuRes);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    /**
     * 设置主菜单的背景色，以及打开/关闭的图标
     *
     * @param mainMenuColor   主菜单背景色
     * @param openMenuBitmap  菜单打开图标，Bitmap 格式
     * @param closeMenuBitmap 菜单关闭图标，Bitmap 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu setMainMenu(int mainMenuColor, PixelMap openMenuBitmap, PixelMap closeMenuBitmap) {
        openMenuIcon = convertBitmap(openMenuBitmap);
        closeMenuIcon = convertBitmap(closeMenuBitmap);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    /**
     * 设置主菜单的背景色，以及打开/关闭的图标
     *
     * @param mainMenuColor     主菜单背景色
     * @param openMenuDrawable  菜单打开图标，Drawable 格式
     * @param closeMenuDrawable 菜单关闭图标，Drawable 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu setMainMenu(int mainMenuColor, PixelMapElement openMenuDrawable, PixelMapElement closeMenuDrawable) {
        openMenuIcon = openMenuDrawable;
        closeMenuIcon = closeMenuDrawable;
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    /**
     * 添加一个子菜单项，包括子菜单的背景色以及图标
     *
     * @param menuColor 子菜单的背景色
     * @param menuRes   子菜单图标，Resource 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu addSubMenu(int menuColor, int menuRes) {
        if (subMenuColorList.size() < MAX_SUBMENU_NUM && subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuColorList.add(menuColor);
            subMenuDrawableList.add(convertDrawable(menuRes));
            itemNum = Math.min(subMenuColorList.size(), subMenuDrawableList.size());
        }
        return this;
    }

    /**
     * 添加一个子菜单项，包括子菜单的背景色以及图标
     *
     * @param menuColor  子菜单的背景色
     * @param menuBitmap 子菜单图标，Bitmap 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu addSubMenu(int menuColor, PixelMap menuBitmap) {
        if (subMenuColorList.size() < MAX_SUBMENU_NUM && subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuColorList.add(menuColor);
            subMenuDrawableList.add(convertBitmap(menuBitmap));
            itemNum = Math.min(subMenuColorList.size(), subMenuDrawableList.size());
        }
        return this;
    }

    /**
     * 添加一个子菜单项，包括子菜单的背景色以及图标
     *
     * @param menuColor    子菜单的背景色
     * @param menuDrawable 子菜单图标，Drawable 格式
     * @return CircleMenu CircleMenu
     */
    public CircleMenu addSubMenu(int menuColor, PixelMapElement menuDrawable) {
        if (subMenuColorList.size() < MAX_SUBMENU_NUM && subMenuDrawableList.size() < MAX_SUBMENU_NUM) {
            subMenuColorList.add(menuColor);
            subMenuDrawableList.add(menuDrawable);
            itemNum = Math.min(subMenuColorList.size(), subMenuDrawableList.size());
        }
        return this;
    }

    /**
     * 打开菜单
     * Open the CircleMenu
     */
    public void openMenu() {
        if (status == STATUS_MENU_CLOSED) {
            status = STATUS_MENU_OPEN;
            startOpenMenuAnima();
        }
    }

    /**
     * 关闭菜单
     * <p>
     * Close the CircleMenu
     */
    public void closeMenu() {
        if (status == STATUS_MENU_OPENED) {
            status = STATUS_MENU_CANCEL;
            startCancelMenuAnima();
        }
    }

    /**
     * 菜单是否关闭
     * Returns whether the menu is alread open
     *
     * @return boolean boolean
     */
    public boolean isOpened() {
        return status == STATUS_MENU_OPENED;
    }

    /**
     * setOnMenuSelectedListener
     *
     * @param listener listener
     * @return this this
     */
    public CircleMenu setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    /**
     * setOnMenuStatusChangeListener
     *
     * @param listener listener
     * @return this this
     */
    public CircleMenu setOnMenuStatusChangeListener(OnMenuStatusChangeListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        final float scale = DisplayManager.getInstance().getDefaultDisplay(getContext()).get().getRealAttributes().densityPixels;
        return (int) (dpValue * scale + 0.5f);
    }


}