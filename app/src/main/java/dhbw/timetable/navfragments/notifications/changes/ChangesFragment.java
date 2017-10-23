package dhbw.timetable.navfragments.notifications.changes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.ListDialog;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class ChangesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_changes, container, false);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        final TextView notCritView = (TextView) view.findViewById(R.id.NotCritText);
        final TextView notCritValueView = (TextView) view.findViewById(R.id.NotCritValue);

        final TextView formView = (TextView) view.findViewById(R.id.FormText);
        final TextView formValueView = (TextView) view.findViewById(R.id.FormValue);

        final TextView toneView = (TextView) view.findViewById(R.id.ToneText);
        final TextView toneValueView = (TextView) view.findViewById(R.id.ToneValue);

        View.OnClickListener onCritClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a criterion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("onChangeCritIndex", which);
                            editor.putString("onChangeCrit", checkedItem);
                            editor.apply();
                            notCritValueView.setText(checkedItem);

                            boolean crit = !checkedItem.equals("None");
                            formView.setEnabled(crit);
                            formValueView.setEnabled(crit);
                            toneView.setEnabled(crit);
                            toneValueView.setEnabled(crit);
                        }
                    }
                }, sharedPref.getInt("onChangeCritIndex", 0), "None", "One week ahead", "Every change")
                        .show(getActivity().getFragmentManager(), "changes_crit");
            }
        };

        notCritValueView.setText(sharedPref.getString("onChangeCrit", "None"));
        notCritValueView.setOnClickListener(onCritClick);
        final boolean checked = !notCritValueView.getText().equals("None");

        notCritView.setOnClickListener(onCritClick);

        View.OnClickListener onFormClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a form", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("onChangeFormIndex", which);
                            editor.putString("onChangeForm", checkedItem);

                            // Force one dimension enabled
                            if (checkedItem.equals("None")) {
                                editor.putInt("onChangeToneIndex", 1);
                                editor.putString("onChangeTone", "Default");
                                toneValueView.setText("Default");
                            }

                            editor.apply();
                            formValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("onChangeFormIndex", 0), "Banner", "None")
                        .show(getActivity().getFragmentManager(), "changes_form");
            }
        };

        formView.setEnabled(checked);
        formView.setOnClickListener(onFormClick);

        formValueView.setEnabled(checked);
        formValueView.setText(sharedPref.getString("onChangeForm", "Banner"));
        formValueView.setOnClickListener(onFormClick);

        View.OnClickListener onToneClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a tone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            // Play the tone
                            if (checkedItem.equals("Default")) {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(ChangesFragment.this.getActivity()
                                        .getApplication().getApplicationContext(), notification);
                                r.play();
                            }
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("onChangeToneIndex", which);
                            editor.putString("onChangeTone", checkedItem);

                            // Force one dimension enabled
                            if (checkedItem.equals("None")) {
                                editor.putInt("onChangeFormIndex", 1);
                                editor.putString("onChangeForm", "Banner");
                                formValueView.setText("Banner");
                            }

                            editor.apply();
                            toneValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("onChangeToneIndex", 0), "None", "Default")
                        .show(getActivity().getFragmentManager(), "changes_tone");
            }
        };

        toneView.setEnabled(checked);
        toneView.setOnClickListener(onToneClick);

        toneValueView.setEnabled(checked);
        toneValueView.setText(sharedPref.getString("onChangeTone", "None"));
        toneValueView.setOnClickListener(onToneClick);
        return view;
    }
}
