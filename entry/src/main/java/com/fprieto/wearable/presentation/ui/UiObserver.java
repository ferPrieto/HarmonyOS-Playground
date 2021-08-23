package com.fprieto.wearable.presentation.ui;

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.abilityjet.activedata.DataObserver;
import ohos.app.dispatcher.TaskDispatcher;

public abstract class UiObserver<T> extends DataObserver<T> {

    private TaskDispatcher uiTaskDispatcher;

    public UiObserver(AbilitySlice slice) {
        setLifecycle(slice.getLifecycle());
        uiTaskDispatcher = slice.getUITaskDispatcher();
    }

    @Override
    public void onChanged(T t) {
        uiTaskDispatcher.asyncDispatch(() -> onValueChanged(t));
    }

    public abstract void onValueChanged(T t);
}
