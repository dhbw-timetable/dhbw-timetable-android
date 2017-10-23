package dhbw.timetable.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import dhbw.timetable.R;

/**
 * Created by Hendrik Ulbich (C) 2017
 */
public class ErrorDialog extends InfoDialog {

    protected String moreMSG;

    /**
     * Create a new instance of ErrorDialog, providing args.
     */
    public static ErrorDialog newInstance(String title, String errMSG, String moreMSG) {
        ErrorDialog errorDialog = new ErrorDialog();
        errorDialog.message = errMSG;
        errorDialog.title = title;
        errorDialog.moreMSG = moreMSG;

        return errorDialog;
    }

    /**
     * Mandatory default constructor
     */
    public ErrorDialog() {
    }

    /**
     * Applies the args
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        // Apply args
        final AlertDialog d = builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("MORE", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                View.OnClickListener onMore = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog alertDialog = ((AlertDialog) dialog);
                        alertDialog.setMessage(ErrorDialog.this.message + "\n\n" + ErrorDialog.this.moreMSG);
                        button.setVisibility(View.GONE);
                    }
                };
                button.setOnClickListener(onMore);
            }
        });


        return d;
    }
}
