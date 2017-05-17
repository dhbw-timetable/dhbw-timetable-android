package dhbw.timetable.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

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

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
