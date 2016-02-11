package octobite.com.br.spriteview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import br.com.octobite.spritelib.SpriteView;


public class MainActivity extends AppCompatActivity {


    private SpriteView mSView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSView = (SpriteView) findViewById(R.id.sprite_view);

        int[] frontAnim = new int[]{0, 1, 2, 3};
        int[] leftAnim = new int[]{4, 5, 6, 7};
        int[] backAnim = new int[]{8, 9, 10, 11};
        int[] rightAnim = new int[]{12, 13, 14, 15};


        mSView.addAnimation("front", frontAnim);
        mSView.addAnimation("left", leftAnim);
        mSView.addAnimation("back", backAnim);
        mSView.addAnimation("right", rightAnim);
        mSView.setDefaultAnimation(frontAnim);
        mSView.startAnim();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    public void startAnim(View v) {
        mSView.startAnim();
    }

    public void stopAnim(View v) {
        mSView.stopAnim();

    }

    public void playFrontAnim(View v) {
        mSView.playAnimation("front");
    }

    public void playLeftAnim(View v) {
        mSView.playAnimarionOnce("left", SpriteView.DEFAULT_ANIMATION);
    }

    public void playBackAnim(View v) {
        mSView.playAnimarionOnce("back", "left");
    }

    public void playRightAnim(View v) {
        mSView.playAnimation("right");
    }

}
