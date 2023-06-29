// Generated by view binder compiler. Do not edit!
package com.karimo.prayertimes.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.karimo.prayertimes.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityInfoBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final TextView TextView00;

  @NonNull
  public final TextView TextView01;

  @NonNull
  public final TextView TextView03;

  @NonNull
  public final TextView TextView04;

  @NonNull
  public final TextView infoTextView;

  private ActivityInfoBinding(@NonNull ScrollView rootView, @NonNull TextView TextView00,
      @NonNull TextView TextView01, @NonNull TextView TextView03, @NonNull TextView TextView04,
      @NonNull TextView infoTextView) {
    this.rootView = rootView;
    this.TextView00 = TextView00;
    this.TextView01 = TextView01;
    this.TextView03 = TextView03;
    this.TextView04 = TextView04;
    this.infoTextView = infoTextView;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityInfoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityInfoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_info, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityInfoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.TextView00;
      TextView TextView00 = ViewBindings.findChildViewById(rootView, id);
      if (TextView00 == null) {
        break missingId;
      }

      id = R.id.TextView01;
      TextView TextView01 = ViewBindings.findChildViewById(rootView, id);
      if (TextView01 == null) {
        break missingId;
      }

      id = R.id.TextView03;
      TextView TextView03 = ViewBindings.findChildViewById(rootView, id);
      if (TextView03 == null) {
        break missingId;
      }

      id = R.id.TextView04;
      TextView TextView04 = ViewBindings.findChildViewById(rootView, id);
      if (TextView04 == null) {
        break missingId;
      }

      id = R.id.infoTextView;
      TextView infoTextView = ViewBindings.findChildViewById(rootView, id);
      if (infoTextView == null) {
        break missingId;
      }

      return new ActivityInfoBinding((ScrollView) rootView, TextView00, TextView01, TextView03,
          TextView04, infoTextView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
