package com.example.capstonemainproject.infra.app;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.widget.TextView;

public class TextHyperLinker {

    public static void makeTextViewHyperLink(TextView view) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(view.getText());
        spannableStringBuilder.setSpan(new URLSpan("#"), 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        view.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
    }
}
