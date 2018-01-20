package com.hm.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hm.wuziqidemo.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/19/019.
 */

public class WuziqiView extends View {

    private Paint mPaint;
    /**
     * 白旗黑旗
     */
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;
    private ArrayList<Point> mWhitePieceArrays = new ArrayList<>();
    private ArrayList<Point> mBlackPieceArrays = new ArrayList<>();
    //控件宽高
    private float mWidth, mHeight;
    //方格尺寸
    private float mLineHeight;
    //棋子宽高为方格宽高的比例
    private final float mRatioPieceOfLineHeight = 3 * 1.0f / 4;
    //棋子尺寸
    private float mPieceSize;
    //view状态恢复与 保存
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_WHITE_PIECE = "instance_white_piece";
    private static final String INSTANCE_BLACK_PIECE = "instance_black_piece";
    private static final String INSTANCE_WHITE_TURN = "instance_white_turn";
    private static final String INSTANCE_GAME_ISOVER = "instance_game_isover";
    /**
     * 标记游戏是否结束
     * 白旗是否获胜
     */
    private boolean mGameIsOver = false;
    private boolean mWhiteIsWin = false;
    /**
     * 自定义属性
     */
    //方格最大数量
    private static final int DEFAULT_MAX_SQUARE = 10;
    private int mLineMaxSquare = DEFAULT_MAX_SQUARE;
    //游戏胜利条件(5子棋)
    private static final int DEFAULT_MAX_CONNECT = 5;
    private int mLineMaxConnect = DEFAULT_MAX_CONNECT;
    //白旗先手
    private static final boolean DEFAULT_WHITE_FIRST = true;
    private boolean mWhiteFirst = DEFAULT_WHITE_FIRST;

    private Context mContext;

    private Drawable mWhitePieceDrawable, mBlackPieceDrawable;

    public WuziqiView(Context context) {
        this(context, null);
    }

    public WuziqiView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WuziqiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttribute(attrs);

//        mWhitePiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.stone_w2);
//        mBlackPiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.stone_b1);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    private void initAttribute(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.WuziqiView);

        mWhitePieceDrawable = array.getDrawable(R.styleable.WuziqiView_wuziqi_view_whitepiece);
        mBlackPieceDrawable = array.getDrawable(R.styleable.WuziqiView_wuziqi_view_blackpiece);
        mLineMaxSquare = array.getInt(R.styleable.WuziqiView_wuziqi_view_maxsquare, mLineMaxSquare);
        mLineMaxConnect = array.getInt(R.styleable.WuziqiView_wuziqi_view_maxconnect, mLineMaxConnect);
        mWhiteFirst = array.getBoolean(R.styleable.WuziqiView_wuziqi_view_whitefirst, mWhiteFirst);
        array.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int finalSize = Math.min(widthSize, heightSize);
        //考虑该控件的 父view是否为scrollview的情况
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            finalSize = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            finalSize = widthSize;
        }

        setMeasuredDimension(finalSize, finalSize);

    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
//        if (drawable instanceof ColorDrawable) {
//            Bitmap bitmap = Bitmap.createBitmap((int) mPieceSize, (int) mPieceSize, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
////            drawable.setBounds(0, 0, (int) mPieceSize, (int) mPieceSize);
////            drawable.draw(canvas);
//            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setDither(true);
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(((ColorDrawable) drawable).getColor());
//            mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
//
//            return bitmap;
//        } else
            if (drawable instanceof BitmapDrawable) {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("请设置棋子颜色或者图片");
        }

    }

    /**
     * view状态保存
     *
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putParcelableArrayList(INSTANCE_WHITE_PIECE, mWhitePieceArrays);
        bundle.putParcelableArrayList(INSTANCE_BLACK_PIECE, mBlackPieceArrays);
        bundle.putBoolean(INSTANCE_WHITE_TURN, mWhiteFirst);
        bundle.putBoolean(INSTANCE_GAME_ISOVER, mGameIsOver);
        return bundle;
    }

    /**
     * view状态恢复
     *
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            super.onRestoreInstanceState(((Bundle) state).getParcelable(INSTANCE));
            mWhitePieceArrays = ((Bundle) state).getParcelableArrayList(INSTANCE_WHITE_PIECE);
            mBlackPieceArrays = ((Bundle) state).getParcelableArrayList(INSTANCE_BLACK_PIECE);
            mWhiteFirst = ((Bundle) state).getBoolean(INSTANCE_WHITE_TURN);
            mGameIsOver = ((Bundle) state).getBoolean(INSTANCE_GAME_ISOVER);
            return;
        }
        super.onRestoreInstanceState(state);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mLineHeight = mWidth / mLineMaxSquare;
        mPieceSize = mLineHeight * mRatioPieceOfLineHeight;

        mWhitePiece = getBitmapFromDrawable(mWhitePieceDrawable);
        mBlackPiece = getBitmapFromDrawable(mBlackPieceDrawable);

        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, (int) (mLineHeight * mRatioPieceOfLineHeight), (int) (mLineHeight * mRatioPieceOfLineHeight), false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, (int) (mLineHeight * mRatioPieceOfLineHeight), (int) (mLineHeight * mRatioPieceOfLineHeight), false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画棋盘
        drawBoard(canvas);
        //画棋子
        drawPiece(canvas);
        //检测游戏结果
        checkGameResult();
    }

    /**
     * 画棋子
     *
     * @param canvas
     */
    private void drawPiece(Canvas canvas) {
        for (int i = 0; i < mWhitePieceArrays.size(); i++) {
            Point p = mWhitePieceArrays.get(i);
            canvas.drawBitmap(mWhitePiece, mLineHeight / 2 - mPieceSize / 2 + mLineHeight * p.x, mLineHeight / 2 - mPieceSize / 2 + mLineHeight * p.y, mPaint);
        }
        for (int i = 0; i < mBlackPieceArrays.size(); i++) {
            Point p = mBlackPieceArrays.get(i);
            canvas.drawBitmap(mBlackPiece, mLineHeight / 2 - mPieceSize / 2 + mLineHeight * p.x, mLineHeight / 2 - mPieceSize / 2 + mLineHeight * p.y, mPaint);
        }
    }

    /**
     * 画棋盘
     *
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {
        for (int i = 0; i < mLineMaxSquare; i++) {
            float startX = mLineHeight / 2;
            float endX = mWidth - mLineHeight / 2;
            canvas.drawLine(startX, mLineHeight / 2 + i * mLineHeight, endX, mLineHeight / 2 + i * mLineHeight, mPaint);
            float startY = mLineHeight / 2;
            float endY = mHeight - mLineHeight / 2;
            canvas.drawLine(mLineHeight / 2 + i * mLineHeight, startY, mLineHeight / 2 + i * mLineHeight, endY, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGameIsOver) return true;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValiPoint(x, y);
            if (mWhitePieceArrays.contains(p) || mBlackPieceArrays.contains(p)) {
                return false;
            }
            if (mWhiteFirst) {
                mWhitePieceArrays.add(p);
            } else {
                mBlackPieceArrays.add(p);
            }
            mWhiteFirst = !mWhiteFirst;

            invalidate();


        }
        return true;
    }

    /**
     * 检测游戏结果
     */
    private void checkGameResult() {
        boolean whiteWinner = checkPiece(mWhitePieceArrays);
        boolean blackWinner = checkPiece(mBlackPieceArrays);

        if (whiteWinner || blackWinner) {
            mGameIsOver = true;
            mWhiteIsWin = whiteWinner;
        }

        if (mGameIsOver) {
            String text = mWhiteIsWin ? "白旗胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }

        //和棋 没有赢家
        if (mWhitePieceArrays.size() + mBlackPieceArrays.size() == Math.pow(mLineMaxSquare, 2)) {
            Toast.makeText(getContext(), "和棋", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkPiece(ArrayList<Point> array) {

        for (Point p : array) {
            boolean isWin = checkHorizontal(p.x, p.y, array);
            if (isWin) return true;
            isWin = checkVertical(p.x, p.y, array);
            if (isWin) return true;
            isWin = checkLeftBottom2rightTop(p.x, p.y, array);
            if (isWin) return true;
            isWin = checkLeftTop2rightBottom(p.x, p.y, array);
            if (isWin) return true;
        }
        return false;
    }


    /**
     * 水平方向找
     *
     * @param x
     * @param y
     * @param arrays
     * @return
     */
    private boolean checkHorizontal(int x, int y, ArrayList<Point> arrays) {
        int count = 1;
        for (int i = 1; i < mLineMaxConnect; i++) { //找左边
            if (arrays.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == mLineMaxConnect) return true;
        for (int i = 1; i < mLineMaxConnect; i++) { //找右边边
            if (arrays.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }

        return count == mLineMaxConnect;
    }

    /**
     * 垂直方向找
     *
     * @param x
     * @param y
     * @param arrays
     * @return
     */
    private boolean checkVertical(int x, int y, ArrayList<Point> arrays) {

        int count = 1;
        for (int i = 1; i < mLineMaxConnect; i++) { //找上边
            if (arrays.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == mLineMaxConnect) return true;
        for (int i = 1; i < mLineMaxConnect; i++) { //找下边
            if (arrays.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        return count == mLineMaxConnect;
    }

    /**
     * 左下至右上
     *
     * @param x
     * @param y
     * @return
     */
    private boolean checkLeftBottom2rightTop(int x, int y, ArrayList<Point> arrays) {
        int count = 1;
        for (int i = 1; i < mLineMaxConnect; i++) { //找左下
            if (arrays.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == mLineMaxConnect) return true;
        for (int i = 1; i < mLineMaxConnect; i++) { //找右上
            if (arrays.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        return count == mLineMaxConnect;
    }

    /**
     * 左上至右下
     *
     * @param x
     * @param y
     * @return
     */
    private boolean checkLeftTop2rightBottom(int x, int y, ArrayList<Point> arrays) {
        int count = 1;
        for (int i = 1; i < mLineMaxConnect; i++) { //找左上
            if (arrays.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == mLineMaxConnect) return true;
        for (int i = 1; i < mLineMaxConnect; i++) { //找右下
            if (arrays.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        return count == mLineMaxConnect;
    }

    //获取合法Point
    private Point getValiPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }
}
