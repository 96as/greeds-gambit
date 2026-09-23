package com.greedsgambit.score;

import java.util.List;

public interface ScoreStore {

    List<BestTime> all();

    void save(BestTime bestTime);
}
