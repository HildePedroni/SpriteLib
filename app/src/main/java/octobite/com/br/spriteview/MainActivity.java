package octobite.com.br.spriteview;

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import br.com.octobite.spritelib.SpriteView;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "SpriteView";
    private SpriteView mSView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSView = (SpriteView) findViewById(R.id.sprite_view);


        int[] animDef = new int[]{0, 1, 2, 3, 4};
        int[] pegaRadio = new int[]{5, 6, 7, 8, 9};
        int[] guardaRadio = new int[]{9, 8, 7, 6, 5};
        int[] talkAudio = new int[]{10, 11, 12, 13, 14};
        int[] vitoria = new int[]{15, 16, 17, 18, 19};
        int[] derrota = new int[]{20, 21, 22, 23, 24};


        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long mem = memoryInfo.availMem;
        Log.d(TAG, "Memory available " + (mem / 1048576L));

        mSView.addAnimation("pegaRadio", pegaRadio);
        mSView.addAnimation("guardaRadio", guardaRadio);
        mSView.addAnimation("audioAtivo", talkAudio);
        mSView.addAnimation("vitoria", vitoria);
        mSView.addAnimation("derrota", derrota);
        mSView.setDefaultAnimation(animDef);
        mSView.startAnim();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    public void pegaRadio(View v) {

        mSView.playAnimationOnce("pegaRadio", "audioAtivo");
    }

    public void guardaRadio(View v) {
        mSView.playAnimationOnce("guardaRadio", SpriteView.DEFAULT_ANIMATION);

    }

    public void playVitoria(View v) {

        mSView.playAnimationOnce("vitoria", SpriteView.DEFAULT_ANIMATION);
    }

    public void playDerrota(View v) {
        mSView.playAnimationOnce("derrota", SpriteView.DEFAULT_ANIMATION);
    }


}
