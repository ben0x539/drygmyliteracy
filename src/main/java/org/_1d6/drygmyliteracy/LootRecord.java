package org._1d6.drygmyliteracy;

import net.minecraft.world.item.ItemStack;

public final class LootRecord {
    public final String entity;
    public final ItemStack stack;
    public int amount;

    public LootRecord(String entity, ItemStack stack) {
        this.entity = entity;
        this.stack = stack;
        this.amount = 0;
    }

    @Override
    public String toString() {
        return "LootRecord[" +
                "entity=" + entity + ", " +
                "stack=" + stack + ", " +
                "amount=" + amount + ']';
    }
}
