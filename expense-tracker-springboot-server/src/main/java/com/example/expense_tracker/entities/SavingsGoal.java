package com.example.expense_tracker.entities;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

// IMPORTANT: adjust the import below if your User entity is in a different package
@ManyToOne(optional = false, fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
private User user;

@Column(nullable = false, length = 90)
private String name;

@Column(name = "target_amount", precision = 15, scale = 2, nullable = false)
private BigDecimal targetAmount = BigDecimal.ZERO;

@Column(name = "current_amount", precision = 15, scale = 2, nullable = false)
private BigDecimal currentAmount = BigDecimal.ZERO;

private LocalDate deadline;

@Column(nullable = false)
private boolean completed = false;

// Getters / Setters
public Integer getId() { return id; }
public void setId(Integer id) { this.id = id; }

public User getUser() { return user; }
public void setUser(User user) { this.user = user; }

public String getName() { return name; }
public void setName(String name) { this.name = name; }

public BigDecimal getTargetAmount() { return targetAmount; }
public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

public BigDecimal getCurrentAmount() { return currentAmount; }
public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

public LocalDate getDeadline() { return deadline; }
public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

public boolean isCompleted() { return completed; }
public void setCompleted(boolean completed) { this.completed = completed; }
}
