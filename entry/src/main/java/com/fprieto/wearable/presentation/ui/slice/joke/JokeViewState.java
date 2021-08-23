package com.fprieto.wearable.presentation.ui.slice.joke;

import com.fprieto.wearable.model.Joke;
import com.fprieto.wearable.presentation.ui.base.BaseViewState;

import java.util.List;

public abstract class JokeViewState extends BaseViewState {
    public static class Loading extends JokeViewState {

    }

    public static class ErrorState extends JokeViewState {
        private final Throwable throwable;

        public ErrorState(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    public static class Loaded extends JokeViewState {
        private final List<Joke> jokes;

        public Loaded(List<Joke> jokes) {
            this.jokes = jokes;
        }

        public List<Joke> getJokes() {
            return jokes;
        }
    }
}
