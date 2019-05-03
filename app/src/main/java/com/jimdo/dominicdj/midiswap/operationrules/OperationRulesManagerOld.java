package com.jimdo.dominicdj.midiswap.operationrules;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class OperationRulesManagerOld {

    private static List<OperationRule> operationRules = new ArrayList<>(5);

    private static final String TAG = OperationRulesManagerOld.class.getSimpleName();

    public static List<OperationRule> getOperationRules() {
        return operationRules;
    }

    public static boolean addOperationRule(OperationRule operationRule) {
        // TODO: take this into consideration for the RecyclerView (coming soon) as well!
        //  If the OperationRule was already added we should show a message to the user.
        for (OperationRule definedOperationRules : operationRules) {
            if (definedOperationRules.equals(operationRule)) {
                Log.d(TAG, "The OperationRule that should have been added was already defined.");
                return false; // the new OperationRule is already defined like it is, so no need to change anything
            }
        }
        return operationRules.add(operationRule);
    }

    public static boolean deleteOperationRule(OperationRule operationRule) {
        // a check is already included in the remove method: it only removes the element, if it is present
        boolean containedSpecifiedElement = operationRules.remove(operationRule);
        Log.d(TAG, "After deleting an OperationRule the operationRules are now: " + operationRules);
        return containedSpecifiedElement;
    }

}
