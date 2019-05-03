package com.jimdo.dominicdj.midiswap.ruleslist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.jimdo.dominicdj.midiswap.R;

public class RulesViewHolder extends RecyclerView.ViewHolder {

    private Spinner spinnerRecv;
    private Spinner spinnerSend;

    public RulesViewHolder(@NonNull final View itemView, final AdapterView.OnItemSelectedListener onItemSelectedListener) {
        super(itemView);
        spinnerRecv = itemView.findViewById(R.id.spinner_recv);
        spinnerRecv.setSelection(0, false); // TODO: do another way, not by guessing numbers
        spinnerSend = itemView.findViewById(R.id.spinner_send);
        spinnerSend.setSelection(2, false);

        spinnerRecv.setOnItemSelectedListener(onItemSelectedListener);
        spinnerSend.setOnItemSelectedListener(onItemSelectedListener);
    }

    /**
     * Bind the data to this ViewHolder.
     * @param viewModel the ViewModel that contains the data
     */
    public void bindData(final RulesViewModel viewModel) {
        // Replace the contents of the view with the help of the element from our dataset.
        spinnerRecv.setAdapter(viewModel.getAdapterRecv());
        spinnerRecv.setTag(R.id.TAG_SPINNER_OPERATION_RULE, viewModel.getOperationRule());
        spinnerSend.setAdapter(viewModel.getAdapterSend());
        spinnerSend.setTag(R.id.TAG_SPINNER_OPERATION_RULE, viewModel.getOperationRule());
    }

}