package com.jimdo.dominicdj.midiswap.ruleslist;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.jimdo.dominicdj.midiswap.Operations;
import com.jimdo.dominicdj.midiswap.R;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelMessage;
import com.jimdo.dominicdj.midiswap.midimessage.MidiConstants;
import com.jimdo.dominicdj.midiswap.operationrules.OperationRule;

import java.util.List;

public class RulesListener implements AdapterView.OnClickListener {

    private static final String TAG = RulesListener.class.getSimpleName();

    // ==========================================================================
    // OnClickListener for 'delete'-button
    // ==========================================================================
    @Override
    public void onClick(View v) {
        // ==========================================================================
        // Get object references.
        // ==========================================================================
        // Get ImageButton.
        if (!(v instanceof ImageButton)) {
            Log.d(TAG, "The view which called this method is no ImageButton.");
            return;
        }
        final ImageButton deleteButton = (ImageButton) v;

        // TODO: add checks
        final int adapterPosition = (int) deleteButton.getTag(R.id.TAG_ADAPTER_POSITION);
        final OperationRule operationRule = (OperationRule) deleteButton.getTag(R.id.TAG_SPINNER_OPERATION_RULE);

        // ==========================================================================
        // Delete item.
        // ==========================================================================
        Operations.getRulesAdapter().removeItem(adapterPosition);

        if (OperationRule.OperationRulesManager.deleteOperationRule(operationRule)) {
            Toast.makeText(v.getContext(), "Rule deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(v.getContext(), "Rule couldn't be deleted", Toast.LENGTH_SHORT).show();
        }
    }

}
