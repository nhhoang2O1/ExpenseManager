package com.example.appquanlychitieu.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "goal_history",
        foreignKeys = @ForeignKey(entity = Goal.class,
                parentColumns = "id",
                childColumns = "goalId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("goalId")})
public class GoalHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long goalId;
    private double amountAdded;
    private long date;

    public GoalHistory(long goalId, double amountAdded, long date) {
        this.goalId = goalId;
        this.amountAdded = amountAdded;
        this.date = date;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getGoalId() { return goalId; }
    public void setGoalId(long goalId) { this.goalId = goalId; }

    public double getAmountAdded() { return amountAdded; }
    public void setAmountAdded(double amountAdded) { this.amountAdded = amountAdded; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
}
