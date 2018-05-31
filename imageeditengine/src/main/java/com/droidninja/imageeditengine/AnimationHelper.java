package com.droidninja.imageeditengine;

import android.content.Context;
import android.support.annotation.AnimRes;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;


/**
 * Created by droidNinja on 03/06/16.
 */
public class AnimationHelper {
    public static void animate(Context context,View view, @AnimRes int anim, int visibility, Animation.AnimationListener animationListener)
    {
        if(view.getVisibility()!=visibility) {
            Animation animation = AnimationUtils.loadAnimation(context, anim);
            animation.setAnimationListener(animationListener);

            view.startAnimation(animation);
            view.setVisibility(visibility);
        }
    }
}
