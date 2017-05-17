package dhbw.timetable.navfragments.notifications;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.ListDialog;

public class ChangesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_changes, container, false);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        final Switch nOCSwitch = (Switch) view.findViewById(R.id.NotifyOnChangeSwitch);

        final TextView formView = (TextView) view.findViewById(R.id.FormText);
        final TextView formValueView = (TextView) view.findViewById(R.id.FormValue);

        final TextView toneView = (TextView) view.findViewById(R.id.ToneText);
        final TextView toneValueView = (TextView) view.findViewById(R.id.ToneValue);

        nOCSwitch.setChecked(sharedPref.getBoolean("notifyOnChange", false));
        nOCSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("notifyOnChange", isChecked);
                editor.apply();

                formView.setEnabled(isChecked);
                formValueView.setEnabled(isChecked);
                toneView.setEnabled(isChecked);
                toneValueView.setEnabled(isChecked);
            }
        });

        View.OnClickListener onFormClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a form", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog)dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("onChangeFormIndex", which);
                            editor.putString("onChangeForm", checkedItem);
                            editor.apply();
                            formValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("onChangeFormIndex", 0),"Banner", "Pop-Up", "None")
                        .show(getActivity().getFragmentManager(), "changes_form");
            }
        };

        formView.setEnabled(nOCSwitch.isChecked());
        formView.setOnClickListener(onFormClick);

        formValueView.setEnabled(nOCSwitch.isChecked());
        formValueView.setText(sharedPref.getString("onChangeForm", "Banner"));
        formValueView.setOnClickListener(onFormClick);

        View.OnClickListener onToneClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a tone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog)dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            // TODO Play the tone
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("onChangeToneIndex", which);
                            editor.putString("onChangeTone", checkedItem);
                            editor.apply();
                            toneValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("onChangeToneIndex", 0),"Blub", "Pieps", "Tzz", "None")
                        .show(getActivity().getFragmentManager(), "changes_tone");
            }
        };

        toneView.setEnabled(nOCSwitch.isChecked());
        toneView.setOnClickListener(onToneClick);

        toneValueView.setEnabled(nOCSwitch.isChecked());
        toneValueView.setText(sharedPref.getString("onChangeTone", "Blub"));
        toneValueView.setOnClickListener(onToneClick);
        return view;
    }
}
