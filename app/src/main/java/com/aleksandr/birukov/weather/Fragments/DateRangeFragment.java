package com.aleksandr.birukov.weather.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.aleksandr.birukov.weather.Constants;
import com.aleksandr.birukov.weather.R;
import com.savvi.rangedatepicker.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

// описывает логику диалогового окна для выбора периода времени
public class DateRangeFragment extends DialogFragment implements DialogInterface.OnClickListener{
    DateFormat format;
    CalendarPickerView calendar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_date_range, null);
        calendar = (CalendarPickerView) v.findViewById(R.id.calendar_view);

        format = new SimpleDateFormat("dd MMMM");

        final Calendar clndr = Calendar.getInstance();
        clndr.add(Calendar.DATE, 5);
        calendar.init(Calendar.getInstance().getTime(), clndr.getTime())
                .inMode(CalendarPickerView.SelectionMode.RANGE);

        return new AlertDialog.Builder(getActivity())
                .setPositiveButton("Выбрать", DateRangeFragment.this)
                .setView(v)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case Dialog.BUTTON_POSITIVE:
                List<Date> result = calendar.getSelectedDates();

                if(result.size() < 3){
                    Toasty.error(getActivity(), "Ошибка. Выберите минимум 3 дня", Toast.LENGTH_LONG, true).show();
                    break;
                }

                Date start = result.get(0);
                Date end = result.get(result.size()-1);

                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_DIALOG_RESULT_START, start.getTime());
                intent.putExtra(Constants.KEY_DIALOG_RESULT_END, end.getTime());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
