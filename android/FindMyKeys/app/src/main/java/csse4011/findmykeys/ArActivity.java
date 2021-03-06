/**
 * Copyright 2014 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csse4011.findmykeys;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewOutlineProvider;
import android.view.WindowInsets;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import csse4011.findmykeys.ui.AnimatedPathView;
import csse4011.findmykeys.ui.TransitionAdapter;

public class ArActivity extends Activity {


    private String TAG = "ArActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ArActivity", "created");
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        getWindow().setExitTransition(new Explode());


        setContentView(R.layout.activity_ar);

        Bitmap photo = setupPhoto(getIntent().getIntExtra("photo", R.drawable.photo1));

        colorize(photo);

        setupText();

        setOutlines(R.id.star, R.id.info);
        applySystemWindowsBottomInset(R.id.container);


        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                ImageView hero = (ImageView) findViewById(R.id.photo);
                ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                        getResources().getColor(R.color.photo_tint), 0);
                color.start();

                findViewById(R.id.info).animate().alpha(1.0f);
                findViewById(R.id.star).animate().alpha(1.0f);

                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }



    @Override
    public void onBackPressed() {

        ImageView hero = (ImageView) findViewById(R.id.photo);
        ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                0, getResources().getColor(R.color.photo_tint));
        color.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }
        });
        color.start();

        findViewById(R.id.info).animate().alpha(0.0f);
        findViewById(R.id.star).animate().alpha(0.0f);
    }

    private void setupText() {
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(getIntent().getStringExtra("title"));

        TextView descriptionView = (TextView) findViewById(R.id.description);
        descriptionView.setText("Short distance localisation using augmented and virtual reality.");
    }



    private void setOutlines(int star, int info) {
        final int size = getResources().getDimensionPixelSize(R.dimen.floating_button_size);

        final ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, size, size);
            }
        };

        findViewById(star).setOutlineProvider(vop);
        findViewById(info).setOutlineProvider(vop);
    }

    private void applySystemWindowsBottomInset(int container) {
        View containerView = findViewById(container);
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    view.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void colorize(Bitmap photo) {
        Palette palette = Palette.generate(photo);
        applyPalette(palette);
    }

    private void applyPalette(Palette palette) {
        getWindow().setBackgroundDrawable(new ColorDrawable(palette.getDarkMutedColor().getRgb()));

        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setTextColor(palette.getVibrantColor().getRgb());

        TextView descriptionView = (TextView) findViewById(R.id.description);
        descriptionView.setTextColor(palette.getLightVibrantColor().getRgb());

        colorRipple(R.id.info, palette.getDarkMutedColor().getRgb(),
                palette.getDarkVibrantColor().getRgb());
        colorRipple(R.id.star, palette.getMutedColor().getRgb(),
                palette.getVibrantColor().getRgb());

        View infoView = findViewById(R.id.information_container);
        infoView.setBackgroundColor(palette.getLightMutedColor().getRgb());

        AnimatedPathView star = (AnimatedPathView) findViewById(R.id.star_container);
        star.setFillColor(palette.getVibrantColor().getRgb());
        star.setStrokeColor(palette.getLightVibrantColor().getRgb());
    }

    private void colorRipple(int id, int bgColor, int tintColor) {
        View buttonView = findViewById(id);

        RippleDrawable ripple = (RippleDrawable) buttonView.getBackground();
        GradientDrawable rippleBackground = (GradientDrawable) ripple.getDrawable(0);
        rippleBackground.setColor(bgColor);

        ripple.setColor(ColorStateList.valueOf(tintColor));
    }

    private Bitmap setupPhoto(int resource) {
        Bitmap bitmap = MainActivity.sPhotoCache.get(resource);
        ((ImageView) findViewById(R.id.photo)).setImageBitmap(bitmap);
        return bitmap;
    }

    public void showStar(View view) {

//        toggleStarView();
    }

    private void toggleStarView() {
        final AnimatedPathView starContainer = (AnimatedPathView) findViewById(R.id.star_container);

        if (starContainer.getVisibility() == View.INVISIBLE) {
            findViewById(R.id.photo).animate().alpha(0.2f);
            starContainer.setAlpha(1.0f);
            starContainer.setVisibility(View.VISIBLE);
            starContainer.reveal();
        } else {
            findViewById(R.id.photo).animate().alpha(1.0f);
            starContainer.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    starContainer.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void showInformation(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        toggleInformationView(view);
    }

    private void toggleInformationView(View view) {
        final View infoContainer = findViewById(R.id.information_container);

        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
        float radius = Math.max(infoContainer.getWidth(), infoContainer.getHeight()) * 2.0f;

        Animator reveal;
        if (infoContainer.getVisibility() == View.INVISIBLE) {
            infoContainer.setVisibility(View.VISIBLE);

            reveal = ViewAnimationUtils.createCircularReveal(
                    infoContainer, cx, cy, 0, radius);
            reveal.setInterpolator(new AccelerateInterpolator(2.0f));

//        } else {
//            reveal = ViewAnimationUtils.createCircularReveal(
//                    infoContainer, cx, cy, radius, 0);
//            reveal.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    infoContainer.setVisibility(View.INVISIBLE);
//                }
//            });
//            reveal.setInterpolator(new DecelerateInterpolator(2.0f));

            reveal.setDuration(600);
            reveal.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }


}
