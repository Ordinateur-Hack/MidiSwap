package com.jimdo.dominicdj.midiswap;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class OperationRulesManager {

    private static List<OperationRule> operationRules = new ArrayList<>();

    private static final String TAG = OperationRulesManager.class.getSimpleName();

    public static List<OperationRule> getOperationRules() {
        return operationRules;
    }

    public static boolean addOperationRule(OperationRule operationRule) {
        for (OperationRule definedOperationRules : operationRules) {
            if (definedOperationRules.equals(operationRule)) {
                Log.d(TAG, "The OperationRule that should have been added was already defined.");
                return false; // the new OperationRule is already defined like it is, so no need to change anything
            }
        }
        return operationRules.add(operationRule);
    }

    public static void deleteOperationRule(OperationRule operationRule) {
        operationRules.remove(operationRule);
        Log.d(TAG, "After deleting an OperationRule the operationRules are now: " + operationRules);
    }

}
