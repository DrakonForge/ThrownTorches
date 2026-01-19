package io.github.drakonforge.throwntorches.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.drakonforge.throwntorches.component.PlaceOnGround;
import java.util.HashSet;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RegisterPlaceOnGroundSystem extends HolderSystem<EntityStore> {
    private final ComponentType<EntityStore, ItemComponent> itemComponentType;
    private final HashSet<String> handheldTorchItems;

    public RegisterPlaceOnGroundSystem(ComponentType<EntityStore, ItemComponent> itemComponentType, BlockTypeListAsset handheldTorchBlockTypeListAsset) {
        if (itemComponentType == null) {
            throw new IllegalArgumentException();
        }
        this.itemComponentType = itemComponentType;
        if (handheldTorchBlockTypeListAsset == null) {
            throw new IllegalArgumentException();
        }
        handheldTorchItems = handheldTorchBlockTypeListAsset.getBlockTypeKeys();
    }
    @Override
    public void onEntityAdd(@NonNullDecl Holder<EntityStore> holder,
            @NonNullDecl AddReason addReason, @NonNullDecl Store<EntityStore> store) {
        ItemComponent itemComponent = holder.getComponent(itemComponentType);
        assert itemComponent != null;
        ItemStack stack = itemComponent.getItemStack();
        if (stack != null && handheldTorchItems.contains(stack.getBlockKey()) && stack.getQuantity() == 1) {
            holder.ensureComponent(PlaceOnGround.getComponentType());
        }
    }

    @Override
    public void onEntityRemoved(@NonNullDecl Holder<EntityStore> holder,
            @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<EntityStore> store) {

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(itemComponentType, Query.not(PlaceOnGround.getComponentType()), Query.not(PreventItemMerging.getComponentType()),
                TransformComponent.getComponentType(), Velocity.getComponentType());
    }
}
