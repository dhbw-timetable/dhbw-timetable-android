package dhbw.timetable.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import dhbw.timetable.R;

/**
 * Created by Hendrik Ulbich (C) 2017
 */
public class InfoDialog extends DialogFragment {

    private String message, title;

    /**
     * Create a new instance of ListDialog, providing args.
     */
    public static InfoDialog newInstance(String title, String message) {
        InfoDialog f = new InfoDialog();
        f.message = message;
        f.title = title;
        return f;
    }

    /**
     * Mandatory default constructor
     */
    public InfoDialog() {}

    /**
     * Applies the args
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        // Apply args
        builder.setTitle(title).setMessage(message);

        Dialog d = builder.create();

        return d;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
