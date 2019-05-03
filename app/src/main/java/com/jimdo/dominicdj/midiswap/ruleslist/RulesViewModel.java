package com.jimdo.dominicdj.midiswap.ruleslist;

import com.jimdo.dominicdj.midiswap.operationrules.OperationRule;

/**
 * A ViewModel to hold and modify the data passed to it.
 */
public class RulesViewModel {

    private OperationRule operationRule;
    private SpinnerAdapter adapterRecv;
    private SpinnerAdapter adapterSend;

    public RulesViewModel(OperationRule operationRule, SpinnerAdapter adapterRecv, SpinnerAdapter adapterSend) {
        this.operationRule = operationRule;
        this.adapterRecv = adapterRecv;
        this.adapterSend = adapterSend;
    }

    public SpinnerAdapter getAdapterRecv() {
        return adapterRecv;
    }

    public SpinnerAdapter getAdapterSend() {
        return adapterSend;
    }

    public OperationRule getOperationRule() {
        return operationRule;
    }

}
