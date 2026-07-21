package com.citeconomy.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Company {
    private final UUID id;
    private String name;
    private UUID owner;
    private int balance;
    private int salary = 100;
    private final Set<UUID> employees = new HashSet<>();

    public Company(UUID id, String name, UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.balance = 0;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public int getBalance() { return balance; }
    public int getSalary() { return salary; }
    public Set<UUID> getEmployees() { return employees; }

    void setBalance(int amount) { this.balance = amount; }
    public void addBalance(int amount) { this.balance += amount; }
    public boolean removeBalance(int amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public void setSalary(int salary) {
        this.salary = Math.max(0, salary);
    }

    public boolean deposit(int amount) {
        if (amount <= 0) return false;
        this.balance += amount;
        return true;
    }

    public boolean withdraw(int amount) {
        return removeBalance(amount);
    }

    public void addEmployee(UUID employee) { this.employees.add(employee); }
    public void removeEmployee(UUID employee) { this.employees.remove(employee); }
    public boolean isEmployee(UUID employee) { return this.employees.contains(employee); }
    public boolean isOwner(UUID player) { return this.owner.equals(player); }
    public int getEmployeeCount() { return employees.size(); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        tag.putUUID("Owner", owner);
        tag.putInt("Balance", balance);
        tag.putInt("Salary", salary);

        ListTag employeeList = new ListTag();
        for (UUID employee : employees) {
            CompoundTag empTag = new CompoundTag();
            empTag.putUUID("UUID", employee);
            employeeList.add(empTag);
        }
        tag.put("Employees", employeeList);
        return tag;
    }

    public static Company load(CompoundTag tag) {
        UUID id = tag.getUUID("Id");
        String name = tag.getString("Name");
        UUID owner = tag.getUUID("Owner");
        Company company = new Company(id, name, owner);
        company.setBalance(tag.getInt("Balance"));
        if (tag.contains("Salary")) {
            company.setSalary(tag.getInt("Salary"));
        }

        ListTag employeeList = tag.getList("Employees", Tag.TAG_COMPOUND);
        for (int i = 0; i < employeeList.size(); i++) {
            company.addEmployee(employeeList.getCompound(i).getUUID("UUID"));
        }
        return company;
    }
}
