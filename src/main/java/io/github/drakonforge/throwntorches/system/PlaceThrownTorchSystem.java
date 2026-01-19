package io.github.drakonforge.throwntorches.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.BlockPlacementHelper;
import io.github.drakonforge.throwntorches.component.PlaceOnGround;
import io.github.drakonforge.throwntorches.util.BlockHelpers;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlaceThrownTorchSystem extends EntityTickingSystem<EntityStore> {
    private static final double PLACE_VELOCITY_THRESHOLD = -10.0;

    @Override
    public void tick(float v, int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Velocity velocityComponent = archetypeChunk.getComponent(i, Velocity.getComponentType());
        PlaceOnGround placeOnGroundComponent = archetypeChunk.getComponent(i, PlaceOnGround.getComponentType());
        assert velocityComponent != null;
        assert placeOnGroundComponent != null;

        double currentVelocityY = velocityComponent.getY();
        double previousVelocityY = placeOnGroundComponent.getPreviousVelocityY();
        placeOnGroundComponent.setPreviousVelocityY(currentVelocityY);

        // Check if entity fell to a stop
        if (currentVelocityY != 0.0 || previousVelocityY > PLACE_VELOCITY_THRESHOLD) {
            return;
        }

        TransformComponent transformComponent = archetypeChunk.getComponent(i, TransformComponent.getComponentType());
        ItemComponent itemComponent = archetypeChunk.getComponent(i, ItemComponent.getComponentType());
        assert transformComponent != null;
        assert itemComponent != null;
        ItemStack stack = itemComponent.getItemStack();
        if (stack == null || stack.getQuantity() != 1) {
            return;
        }
        int blockId = BlockHelpers.getBlockId(stack.getBlockKey());

        // Check block beneath
        World world = store.getExternalData().getWorld();
        Vector3d pos = transformComponent.getPosition();
        BlockType belowBlock = BlockHelpers.getBlockType(BlockHelpers.getBlock(world, pos.x, pos.y - 0.1, pos.z));
        BlockType currentBlock = BlockHelpers.getBlockType(BlockHelpers.getBlock(world, pos.x, pos.y, pos.z));
        // BlockPlacementHelper.testSupportingBlock is not perfect, but makes it so it'll never place incorrectly
        // Some cases are missing though
        if (currentBlock == null || belowBlock == null || currentBlock != BlockType.EMPTY || !BlockPlacementHelper.testSupportingBlock(belowBlock, 0, 0)) {
            return;
        }

        // Attempt to place block
        boolean result = BlockHelpers.setBlock(world, pos.x, pos.y, pos.z, blockId);
        if (!result) {
            return;
        }

        // Place and play effects
        commandBuffer.removeEntity(archetypeChunk.getReferenceTo(i), RemoveReason.REMOVE);
        int index = SoundEvent.getAssetMap().getIndex("SFX_Torch_Build");
        SoundUtil.playSoundEvent3d(
                index,
                SoundCategory.SFX,
                pos.x, pos.y, pos.z,
                world.getEntityStore().getStore()
        );

        // TODO: Play sound
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(ItemComponent.getComponentType(), PlaceOnGround.getComponentType(),
                TransformComponent.getComponentType(), Velocity.getComponentType());
    }
}
