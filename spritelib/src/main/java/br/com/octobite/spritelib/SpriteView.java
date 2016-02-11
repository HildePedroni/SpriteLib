package br.com.octobite.spritelib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hildebrando Demestres Pedroni on 2/10/16.
 */


public class SpriteView extends View {


    public static String DEFAULT_ANIMATION = "default";

    private Bitmap mBitmap;
    private Loop mLoop;
    private int frameWidth;
    private int frameHeight;
    private int currentFrame = 0;
    private int rows;
    private int columns;
    private int framesPerSecond = 10;
    private int[] currentAnimation;
    private boolean animating = false;
    private boolean paused = false;
    private List<Point> framePositions;
    private Map<String, int[]> animations;


    public SpriteView(Context context) {
        super(context);
        initSpriteView();
    }

    public SpriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpriteView);

        rows = a.getInt(R.styleable.SpriteView_rows, 1);
        columns = a.getInt(R.styleable.SpriteView_columns, 4);

        int fps = a.getInt(R.styleable.SpriteView_fps, 12);
        setFPS(fps);
        Drawable drawable = a.getDrawable(R.styleable.SpriteView_image);
        if (drawable != null) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        frameWidth = mBitmap.getWidth() / columns;
        frameHeight = mBitmap.getHeight() / rows;
        initSpriteView();
    }

    public SpriteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSpriteView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getLayoutParams().width = frameWidth;
        getLayoutParams().height = frameHeight;//dpToPx(frameHeight, getContext());

    }

    /*
        initiate the components and set the default animation.
        It slices the image, setting the x and y positions of each frame, then put every Point into
        an ArrayList wich will be used to iterate over frames.
     */
    private void initSpriteView() {
        framePositions = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Point p = new Point(x, y);
                framePositions.add(p);
                x += frameWidth;
            }
            x = 0;
            y += frameHeight;
        }

        animations = new HashMap<>();

        int[] defAnimation = new int[framePositions.size()];
        for (int i = 0; i <= framePositions.size() - 1; i++) {
            defAnimation[i] = i;
        }
        //set default animation
        animations.put(DEFAULT_ANIMATION, defAnimation);
        currentAnimation = animations.get(DEFAULT_ANIMATION);
    }

    /**
     * If you are playing animations when call it, remember to start animations again
     *
     * @param frames
     */
    public void setDefaultAnimation(int[] frames) {
        //We cant change default animation wile playing
        if (animating) {
            stopAnim();
        }
        animations.remove(DEFAULT_ANIMATION);
        animations.put(DEFAULT_ANIMATION, frames);
        currentAnimation = animations.get(DEFAULT_ANIMATION);
    }

    /**
     * Set the fps of the animation
     *
     * @param fps
     */
    public void setFPS(int fps) {
        framesPerSecond = 1000 / fps;
    }

    /**
     * if we are not animating, then we can set a frame do be displayed
     *
     * @param frame
     */

    public void setFrame(int frame) {
        if (!animating) {
            if (frame < framePositions.size()) {
                currentFrame = frame;
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Point p = framePositions.get(currentFrame);
        int srcX = p.x;
        int srcY = p.y;
        Rect src = new Rect(srcX, srcY, srcX + frameWidth, srcY + frameHeight);
        Rect dst = new Rect(0, 0, frameWidth, frameHeight);
        canvas.drawBitmap(mBitmap, src, dst, null);
        invalidate();
    }

    /**
     * Start the animation
     */
    public void startAnim() {
        animating = true;
        if (mLoop == null) {
            mLoop = new Loop(this);
            new Thread(mLoop).start();
        }
    }

    /**
     * Stop any animation
     */
    public void stopAnim() {
        playAnimation("default");
        animating = false;
        setFrame(0);
        mLoop = null;
    }


    private int nextFrameToPlay = 0;

    private boolean startCicle = true;

    private void playNextFrame() {

        currentFrame = currentAnimation[nextFrameToPlay];
        nextFrameToPlay++;
        if (startCicle) {
            startCicle = false;
            if (isPlayingOnce) {
                isPlayingOnce = false;
                playAnimation(animationInMemory);
            }
        }
        if (nextFrameToPlay > currentAnimation.length - 1) {
            nextFrameToPlay = 0;
            startCicle = true;
        }


    }

    /**
     * Add a new set of animation to the animation state machine
     *
     * @param animationName
     * @param frames
     */
    public void addAnimation(String animationName, int[] frames) {
        animations.put(animationName, frames);
    }

    /**
     * Pause the current animation, change and start the new one
     *
     * @param animationName
     */
    public void playAnimation(String animationName) {
        paused = true;
        currentAnimation = animations.get(animationName);
        nextFrameToPlay = 0;
        paused = false;
    }

    private String animationInMemory;
    boolean isPlayingOnce = false;


    public void playAnimarionOnce(String animationName, String animationAfter) {
        paused = true;
        animationInMemory = animationAfter;
        currentAnimation = animations.get(animationName);
        nextFrameToPlay = 0;
        isPlayingOnce = true;
        paused = false;

    }

    /*
     * Our loop
     * This inner class controls the animation loop
     */
    class Loop implements Runnable {

        private SpriteView sView;
        private long startTime;


        Loop(SpriteView sView) {
            this.sView = sView;
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            while (animating) {
                if (!paused) {
                    long now = System.currentTimeMillis();
                    if (now > startTime + framesPerSecond) {
                        startTime = now;
                        sView.playNextFrame();
                    }
                }
            }
        }
    }

}
