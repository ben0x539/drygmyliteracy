package org._1d6.drygmyliteracy.mixin;

import com.hollingsworth.arsnouveau.common.block.tile.DrygmyTile;
import com.hollingsworth.arsnouveau.common.block.tile.SummoningTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org._1d6.drygmyliteracy.BookWriter;
import org._1d6.drygmyliteracy.DrygmyLiteracy;
import org._1d6.drygmyliteracy.LootRecord;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@Mixin(value = DrygmyTile.class, remap = false)
public abstract class DrygmyTileMixin extends SummoningTile {
    @Unique
    private ArrayList<LootRecord> lootTableEntries;
    @Unique
    private String currentEntity;

//    @Inject(method = "generateItems", at = @At(value = "HEAD"))
//    private void generateItemsStart(CallbackInfo ci) {
//        DrygmyLiteracy.LOGGER.debug("in generateItemsStart");
//        lootTableEntries = new ArrayList<>();
//    }

    @Redirect(
            method = "generateItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getLootTable()Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<LootTable> generateItemsUpdateEntity(LivingEntity entity) {
        currentEntity = entity.getName().getString();
        DrygmyLiteracy.LOGGER.debug("in generateItemsUpdateEntity: {}", currentEntity);
        return entity.getLootTable();
    }

    @ModifyArg(
            method = "generateItems",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"
            )
    )
    private Collection<ItemStack> generateItemsAddStacks(Collection<ItemStack> collection) {
        DrygmyLiteracy.LOGGER.debug("in generateItemsAddStacks");
        if (currentEntity != null) {
            if (lootTableEntries == null) {
                lootTableEntries = new ArrayList<>();
            }
            for (ItemStack stack : collection) {
                lootTableEntries.add(new LootRecord(currentEntity, stack));
            }
        }

        return collection;
    }

    @ModifyArg(
            method = "generateItems",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;get(I)Ljava/lang/Object;"
            )
    )
    private int generateItemsAddLoot(int index) {
        DrygmyLiteracy.LOGGER.debug("in generateItemsAddLoot");
        if (lootTableEntries != null && lootTableEntries.size() > index) {
            lootTableEntries.get(index).amount += 1;
        } else {
            DrygmyLiteracy.LOGGER.error("generateItemsAddLoot: huh no loot table entries");
        }
        return index;
    }

    @Inject(
            method = "generateItems",
            at = @At(value = "TAIL")
    )
    private void generateItemsRecord(CallbackInfo ci) {
        DrygmyLiteracy.LOGGER.debug("in generateItemsRecord");
        if (lootTableEntries == null || lootTableEntries.isEmpty()) {
            return;
        }

        // reset state early so we can return whenever we want
        ArrayList<LootRecord> entries = lootTableEntries;
        lootTableEntries = null;
        currentEntity = null;

        ItemStack book = null;
        var pos = getBlockPos();
        if (level == null) { return; }
        for (Direction d : Direction.values()) {
            var adj = pos.relative(d);
            if (level.getBlockEntity(adj) instanceof LecternBlockEntity lectern) {
                ItemStack stack = lectern.getBook();
                if (stack.isEmpty()) { continue; }
                if (stack.is(Items.WRITABLE_BOOK)) {
                    book = stack;
                    break;
                }
            }
        }

        if (book == null) {
            DrygmyLiteracy.LOGGER.debug("generateItemsRecord found no book in lectern");
            return;
        }

        BookWriter w = new BookWriter(book);

        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        DrygmyLiteracy.LOGGER.debug("Received loot at {}", now);
        w.writeLine("Items obtained at " + now);
        for (LootRecord r : entries) {
            if (r.amount == 0 || r.stack.isEmpty()) {
                continue;
            }
            var total = r.amount * r.stack.getCount();
            var item = r.stack.getHoverName().getString();
            DrygmyLiteracy.LOGGER.debug("  From {}: {}x {}", r.entity, total, item);
            boolean ok = w.writeLine(String.format("From %s: %dx %s", r.entity, total, item));
            if (!ok) { // book looks full
                DrygmyLiteracy.LOGGER.debug("book full! ending early!");
                break;
            }
        }
        w.update(book);
    }

    // I assume this needs to exist for the super class but is never called
    public DrygmyTileMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        DrygmyLiteracy.LOGGER.debug("in the constructor for some reason");
    }
}
