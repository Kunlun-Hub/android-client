package io.cloink.client.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import io.cloink.client.databinding.LayoutZeroPeerBinding;

class ZeroPeerView {
    public static void setupLearnWhyClick(LayoutZeroPeerBinding binding, Context context) {
        binding.btnLearnWhy.setOnClickListener(v -> {
            String url = "https://one.4w.ink";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });
    }

    public static void updateVisibility(LayoutZeroPeerBinding zeroPeerBinding, View payloadView, Boolean show) {
        if (show) {
            zeroPeerBinding.getRoot().setVisibility(View.GONE);
            payloadView.setVisibility(View.VISIBLE);
        } else {
            zeroPeerBinding.getRoot().setVisibility(View.VISIBLE);
            payloadView.setVisibility(View.GONE);
        }
    }
}
