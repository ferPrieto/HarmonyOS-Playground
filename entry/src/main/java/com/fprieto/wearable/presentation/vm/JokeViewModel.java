package com.fprieto.wearable.presentation.vm;

import com.fprieto.wearable.MyApplication;
import com.fprieto.wearable.presentation.ui.slice.joke.JokeViewState;
import com.fprieto.wearable.presentation.vm.base.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class JokeViewModel extends BaseViewModel<JokeViewState> {

    public void fetchAJoke() {
        final Observable<JokeViewState> obs = MyApplication.getApiService()
                .fetchJokes()
                .map(JokeViewState.Loaded::new)
                .cast(JokeViewState.class)
                .onErrorReturn(JokeViewState.ErrorState::new)
                .subscribeOn(Schedulers.io())
                .startWith(new JokeViewState.Loading());

        super.subscribe(obs);
    }
}
