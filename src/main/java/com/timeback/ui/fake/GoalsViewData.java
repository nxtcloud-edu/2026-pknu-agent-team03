package com.timeback.ui.fake;

import com.timeback.ui.domain.model.GoalProgress;
import java.util.List;

public class GoalsViewData {
    private final List<GoalProgress> goals;

    public GoalsViewData(List<GoalProgress> goals) {
        this.goals = goals;
    }

    public List<GoalProgress> getGoals() { return goals; }
}
