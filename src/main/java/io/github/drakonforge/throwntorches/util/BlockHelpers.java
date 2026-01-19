package io.github.drakonforge.throwntorches.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.packets.world.ServerSetBlock;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// Adapted from https://github.com/ShaneeexD/HytaleDevLib
public final class BlockHelpers {

    public static final int MIN_WORLD_HEIGHT = 0;
    public static final int MAX_WORLD_HEIGHT = 320;

    private BlockHelpers() {}

    /**
     * Get the block name (ID string) from a numeric block ID.
     *
     * @param blockId The numeric block ID
     * @return The block name/ID string (e.g., "hytale:blocks/stone"), or null if not found
     */
    public static String getBlockName(int blockId) {
        BlockType blockType = getBlockType(blockId);
        return blockType != null ? blockType.getId() : null;
    }

    public static BlockType getBlockType(int blockId) {
        return BlockType.getAssetMap().getAsset(blockId);
    }

    /**
     * Get the block ID for a given block name using the game's native BlockType asset map.
     * This allows working with block names instead of numeric IDs.
     *
     * @param blockName The block name (e.g., "Rock_Stone", "Soil_Grass")
     * @return The block ID, or -1 if not found
     */
    public static int getBlockId(String blockName) {
        try {
            int index = BlockType.getAssetMap().getIndex(blockName);
            return index != Integer.MIN_VALUE ? index : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get the block ID at specific coordinates.
     *
     * @param world The world
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return The block ID at those coordinates, or 0 (air) if the chunk is not loaded
     */
    public static int getBlock(World world, int x, int y, int z) {
        if (world == null || y < MIN_WORLD_HEIGHT || y >= MAX_WORLD_HEIGHT) {
            return 0;
        }

        try {
            // Calculate chunk index from block coordinates (Hytale chunks are 32x32)
            long chunkPos = ChunkUtil.indexChunkFromBlock(x, z);

            // Use ChunkStore to get BlockChunk component directly
            ChunkStore chunkStore = world.getChunkStore();
            BlockChunk blockChunk = chunkStore.getChunkComponent(chunkPos, BlockChunk.getComponentType());

            if (blockChunk == null) {
                return 0; // Chunk not accessible
            }

            // Get block within chunk (local coordinates)
            int chunkRelativeX = x & ChunkUtil.SIZE_MASK;
            int chunkRelativeZ = z & ChunkUtil.SIZE_MASK;
            return blockChunk.getBlock(chunkRelativeX, y, chunkRelativeZ);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getBlock(World world, double x, double y, double z) {
        return getBlock(world, MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z));
    }

    /**
     * Set a block at specific coordinates with rotation and filler.
     * This method also sends a ServerSetBlock packet to notify all clients.
     *
     * @param world The world
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param blockId The block ID to set
     * @param rotation Block rotation (0-23, typically 0 for no rotation)
     * @param filler Filler block data (typically 0)
     * @return true if the block was set successfully, false otherwise
     */
    public static boolean setBlock(World world, int x, int y, int z, int blockId, int rotation, int filler) {
        if (world == null || y < MIN_WORLD_HEIGHT || y >= MAX_WORLD_HEIGHT) {
            return false;
        }

        try {
            // Get BlockChunk for this position
            long chunkPos = ChunkUtil.indexChunkFromBlock(x, z);
            ChunkStore chunkStore = world.getChunkStore();
            BlockChunk blockChunk = chunkStore.getChunkComponent(chunkPos, BlockChunk.getComponentType());

            if (blockChunk == null) {
                return false;
            }

            int localX = x & ChunkUtil.SIZE_MASK;
            int localZ = z & ChunkUtil.SIZE_MASK;

            // Set the block in the chunk (this invalidates the section cache)
            boolean success = blockChunk.setBlock(localX, y, localZ, blockId, rotation, filler);

            if (success) {
                // Send ServerSetBlock packet to all players who have this chunk loaded
                sendBlockUpdateToClients(world, x, y, z, blockId, (short) filler, (byte) rotation);
            }

            return success;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean setBlock(World world, int x, int y, int z, int blockId) {
        return setBlock(world, x, y, z, blockId, 0, 0);
    }

    public static boolean setBlock(World world, double x, double y, double z, int blockId) {
        return setBlock(world, MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z), blockId, 0, 0);
    }

    /**
     * Send a block update packet to all clients who have the chunk loaded.
     */
    private static void sendBlockUpdateToClients(World world, int x, int y, int z, int blockId, short filler, byte rotation) {
        try {
            // Create the ServerSetBlock packet
            ServerSetBlock packet = new com.hypixel.hytale.protocol.packets.world.ServerSetBlock(
                    x, y, z, blockId, filler, rotation);

            // Use WorldNotificationHandler to send to all players with this chunk loaded
            world.getNotificationHandler().sendPacketIfChunkLoaded(packet, x, z);
        } catch (Exception e) {
            // Silently fail - block was set but notification failed
        }
    }
}
