package com.example.mealrecmmenderandroid.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mealrecmmenderandroid.R;

public class ImageHelper {

    public static void loadImage(Context context, String url, ImageView imageView) {
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_recipe);
        }
    }

    public static void loadProfileImage(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder);

        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .apply(options)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }
}