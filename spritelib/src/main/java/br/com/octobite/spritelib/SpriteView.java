package br.com.octobite.spritelib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hildebrando Demestres Pedroni on 2/10/16.
 */


public class SpriteView extends View {


    public static String DEFAULT_ANIMATION = "default";

    private Bitmap mBitmap; //The bitmap (spritesheet)
    private Loop mLoop; //runnable tha holds the animation loop
    private int frameWidth; //the widht of one frame. Also the width of the view
    private int frameHeight;//the height of one frame. Also the height of the view
    private int currentFrame; //the actual frame that is being showed
    private int rows; //number of rows of the spritesheet
    private int columns; //number of columns of the spritesheet
    private int framesPerSecond = 10; //the actualization tax
    private int[] currentAnimation; //the current animation(sequence of frames) that is being showed
    private boolean isAnimating = false; //to control the loop

    private List<Point> framePositions; //points that map the spritesheet x,y positions
    private Map<String, int[]> animations; //map with all frame animations represented by a name
    private Rect src; // Rect to extract the frame from the source image
    private Rect dst; //rect to draw the frame
    private String animationInMemory; //Hold the animation that should be played next
    private boolean isPlayingOnce = false;
    private int nextFrameToPlay = 0;
    private boolean isCycleStarting = true;
    private boolean stopAfterPlay = false;

    //Constructors
    public SpriteView(Context context) {
        super(context);

        initSpriteView();
    }

    public SpriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpriteView);

        rows = a.getInt(R.styleable.SpriteView_rows, 1);
        columns = a.getInt(R.styleable.SpriteView_columns, 1);

        int fps = a.getInt(R.styleable.SpriteView_fps, 12);

        setFPS(fps);
        Drawable drawable = a.getDrawable(R.styleable.SpriteView_image);
        if (drawable != null) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
            frameWidth = mBitmap.getWidth() / columns;
            frameHeight = mBitmap.getHeight() / rows;
        }
        a.recycle();
        initSpriteView();
    }

    public SpriteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSpriteView();
    }
    //----

    /*
        initiate the components and set the default animation.
        It slices the image, setting the x and y positions of each frame, then put every Point into
        an ArrayList wich will be used to iterate over frames.
     */
    private void initSpriteView() {
        framePositions = new ArrayList<>();
        mapFrames();
        createDefaultAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (src == null) {
            src = new Rect(0, 0, frameWidth, frameHeight);
            dst = new Rect(0, 0, getWidth(), getHeight());
        }

        Point p = framePositions.get(currentFrame);
        int srcX = p.x;
        int srcY = p.y;
        src.set(srcX, srcY, srcX + frameWidth, srcY + frameHeight);
        canvas.drawBitmap(mBitmap, src, dst, null);
        invalidate();
    }

    /*
        create the map of the x,y positions
     */
    private void mapFrames() {
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
    }

    //Create a default animation, wich is composed of all frames.
    private void createDefaultAnimation() {
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
     * Set the spritesheet.
     * Use this method to initialize the view if you are creating this programatically or
     * If you need to change the sprite sheet at runtime, call this method.
     * If the animation is running, it will be stoped, and you will need to call @startAnim() again
     * <p/>
     * Remember to call @setFPS() if you need to change the speed of the animation
     *
     * @param drawableId
     * @param rows
     * @param columns
     */
    public void setSpriteSheet(int drawableId, int rows, int columns) {
        //We stop the animations to avoid problems, becouse in this case, all parameter could change
        if (isAnimating) {
            stopAnim();
        }
        Drawable mDrawable = ResourcesCompat.getDrawable(getResources(), drawableId, null);
        if (mDrawable != null) {
            mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
            this.rows = rows;
            this.columns = columns;
            frameWidth = mBitmap.getWidth() / columns;
            frameHeight = mBitmap.getHeight() / rows;
            mapFrames();
            createDefaultAnimation();
        } else {
            throw new NullPointerException("Drawable resource not found");
        }

    }


    /**
     * If you are playing animations when call it, remember to start animations again
     *
     * @param frames
     */
    public void setDefaultAnimation(int[] frames) {
        //We cant change default animation wile playing
        if (isAnimating) {
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
     * if we are not isAnimating, then we can set a frame do be displayed
     *
     * @param frame
     */

    public void setFrame(int frame) {
        if (!isAnimating) {
            if (frame < framePositions.size()) {
                currentFrame = frame;
            }
        }
    }


    /**
     * Start the animation
     */
    public void startAnim() {
        isAnimating = true;
        isCycleStarting = true;
        nextFrameToPlay = 0;

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
        isAnimating = false;
        setFrame(0);
        mLoop = null;
    }


    /**
     * Add a new set of animation to the animation state machine
     *
     * @param animationName
     * @param frames
     */
    public void addAnimation(String animationName, int[] frames) {
        if (animationName.equals(DEFAULT_ANIMATION)) {
            throw new InvalidParameterException("Name 'default' is not allowed, use setDefaultAnimation(int[] frames) to set default animation");
        }
        animations.put(animationName, frames);
    }

    /**
     * Pause the current animation, change and start the new one
     *
     * @param animationName
     */
    public void playAnimation(String animationName) {
        if (mLoop == null) {
            currentAnimation = animations.get(animationName);
            startAnim();
        } else {
            mLoop.pause();
            currentAnimation = animations.get(animationName);
            nextFrameToPlay = 0;
            mLoop.unPause();
        }
    }


    /**
     * This method call an animation to be played only one cycle (not in loop)
     * The first param, is the animation to be played. The second one is the animation to be played after the first one
     * The second animation will be played in loop.
     * The general use, is to play a single animation then change to default animation.
     * If you pass a null value to the animationAfter param, the animation will stop on the first frame of the default
     *
     * @param animationName
     * @param animationAfter
     */
    public void playAnimationOnce(String animationName, String animationAfter) {
        if (mLoop == null) {
            startAnim();
        }
        mLoop.pause();
        if (animationAfter == null) {
            stopAfterPlay = true;
        }
        animationInMemory = animationAfter;
        currentAnimation = animations.get(animationName);
        nextFrameToPlay = 0;
        isPlayingOnce = true;
        mLoop.unPause();
    }


    /*
        Update the frames to be played
     */
    private void playNextFrame() {
        if (nextFrameToPlay > currentAnimation.length - 1) {
            nextFrameToPlay = 0;
            isCycleStarting = true;
        }

        if (isCycleStarting) {
            isCycleStarting = false;
            if (isPlayingOnce) {
                isPlayingOnce = false;
                if (stopAfterPlay) {
                    stopAfterPlay = false;
                    stopAnim();
                } else {
                    playAnimation(animationInMemory);
                    animationInMemory = null; //To save memory
                }

            }
        }
        currentFrame = currentAnimation[nextFrameToPlay];
        nextFrameToPlay++;


    }

    /*
     * Our loop
     * This inner class controls the animation loop
     */
    class Loop implements Runnable {

        private SpriteView sView;
        private long startTime;
        private boolean paused = false;


        Loop(SpriteView sView) {
            this.sView = sView;
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            while (isAnimating) {
                if (!paused) {
                    long now = System.currentTimeMillis();
                    if (now > startTime + framesPerSecond) {
                        startTime = now;
                        sView.playNextFrame();
                    }
                }
            }
        }

        public void pause() {
            paused = true;
        }

        public void unPause() {
            paused = false;
            startTime = System.currentTimeMillis();
        }
    }

}