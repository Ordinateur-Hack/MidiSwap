package com.jimdo.dominicdj.midiswap;

import android.util.Log;
import org.jetbrains.annotations.NotNull;

public class OperationRule {

    private String ifRecvMsg;
    private String thenSendMsg;

    public OperationRule(String ifRecvMsg, String thenSendMsg) throws IllegalArgumentException {
        if (ifRecvMsg == null || thenSendMsg == null) {
            throw new IllegalArgumentException("The messages of the OperationRule can't be null.");
        }
        this.ifRecvMsg = ifRecvMsg;
        this.thenSendMsg = thenSendMsg;
    }

    @NotNull
    public String getIfRecvMsg() {
        return ifRecvMsg;
    }

    @NotNull
    public String getThenSendMsg() {
        return thenSendMsg;
    }

    @Override
    public String toString() {
        return "OperationRule[" + ifRecvMsg + ", " + thenSendMsg + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OperationRule) {
            OperationRule rule = (OperationRule) obj;
            if (rule.ifRecvMsg.equals(ifRecvMsg) && rule.thenSendMsg.equals(thenSendMsg)) {
                Log.d("OperationRules", "Equals returned true");
                return true;
            } else {
                Log.d("OperationRules", "Equals returned false");
                return false;
            }
            // return rule.ifRecvMsg.equals(ifRecvMsg) && rule.thenSendMsg.equals(thenSendMsg);
        }
        Log.d("OperationRules", "Equals returned false (end)!");
        return false;
    }

}
