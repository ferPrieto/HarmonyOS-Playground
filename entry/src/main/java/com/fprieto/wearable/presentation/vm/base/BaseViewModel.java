package com.fprieto.wearable.presentation.vm.base;

import com.fprieto.wearable.presentation.ui.base.BaseViewState;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import ohos.aafwk.abilityjet.activedata.ActiveData;

public class BaseViewModel<T extends BaseViewState> {
    public static final String TAG = BaseViewModel.class.getSimpleName();

    protected CompositeDisposable disposable = new CompositeDisposable();
    protected ActiveData<T> states = new ActiveData<>();

    public BaseViewModel( ) {

    }

    public void subscribe(Observable<T> request) {
        disposable.add(request.subscribe(T -> {
            states.setData(T);
        }));
    }

    public void unbind() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }


    public ActiveData<T> getStates() {
        return states;
    }
}
