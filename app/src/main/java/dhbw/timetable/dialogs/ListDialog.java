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
public class ListDialog extends DialogFragment {

    private String title;
    private int active;
    private String[] choices;
    private DialogInterface.OnClickListener onC;

    /**
     * Create a new instance of ListDialog, providing args.
     */
    public static ListDialog newInstance(String title, DialogInterface.OnClickListener onClick,
                                         int active, String... choices) {
        ListDialog f = new ListDialog();

        f.title = title;
        f.choices = choices;
        f.active = active;
        f.onC = onClick;

        return f;
    }

    /**
     * Mandatory default constructor
     */
    public ListDialog() {}

    /**
     * Applies the args
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        // Apply args
        builder.setTitle(title).setSingleChoiceItems(choices, active, onC).setPositiveButton("OK", onC);

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
