package com.jimdo.dominicdj.midiswap;

public class OperationRules {

    // TODO: define rule object, extend for more complex tasks

    private static String ifRecvMsg;
    private static String thenSendMsg;

    public static String[] getRule() {
        return new String[]{ifRecvMsg, thenSendMsg};
    }

    public static void updateRule(String ifRecvMsg, String thenSendMsg) {
        OperationRules.ifRecvMsg = ifRecvMsg;
        OperationRules.thenSendMsg = thenSendMsg;
    }

    public static void updateRuleRecv(String ifRecvMsg) {
        OperationRules.ifRecvMsg = ifRecvMsg;
    }

    public static void updateRuleSend(String thenSendMsg) {
        OperationRules.thenSendMsg = thenSendMsg;
    }

}
