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
public class YNDialog extends DialogFragment {

    private String yM, nM, message;
    private DialogInterface.OnClickListener oY, oN;

    /**
     * Create a new instance of YNDialog, providing args.
     */
    public static YNDialog newInstance(String message, String yes, String no,
                                DialogInterface.OnClickListener onYes,
                                DialogInterface.OnClickListener onNo) {
        YNDialog f = new YNDialog();

        f.message = message;
        f.yM = yes;
        f.nM = no;
        f.oY = onYes;
        f.oN = onNo;

        return f;
    }

    /**
     * Mandatory default constructor
     */
    public YNDialog() {}

    /**
     * Applies the args
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        // Apply args
        builder.setMessage(message).setPositiveButton(yM, oY).setNegativeButton(nM, oN);

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
