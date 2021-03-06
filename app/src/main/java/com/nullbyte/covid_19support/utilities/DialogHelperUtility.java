package com.nullbyte.covid_19support.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.nullbyte.covid_19support.callbacks.ViewCallback;

import java.util.Objects;

public class DialogHelperUtility {

    public static void customDialog(Context context, int layout, ViewCallback viewCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(layout, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        viewCallback.onSuccess(view, dialog);
    }

    public static void customClosableDialog(Context context, int layout, ViewCallback viewCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(layout, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);
        viewCallback.onSuccess(view, dialog);
    }
}
