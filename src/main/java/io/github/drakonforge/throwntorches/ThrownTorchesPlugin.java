package io.github.drakonforge.throwntorches;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.drakonforge.throwntorches.component.PlaceOnGround;
import io.github.drakonforge.throwntorches.system.PlaceThrownTorchSystem;
import io.github.drakonforge.throwntorches.system.RegisterPlaceOnGroundSystem;
import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class ThrownTorchesPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static ThrownTorchesPlugin instance;

    private ComponentType<EntityStore, PlaceOnGround> placeOnGroundComponentType;

    public static ThrownTorchesPlugin getInstance() {
        return instance;
    }

    public ThrownTorchesPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;
        LOGGER.atInfo().log("Setting up plugin " + this.getName() + " v" + this.getManifest().getVersion().toString());

        this.placeOnGroundComponentType = this.getEntityStoreRegistry().registerComponent(
                PlaceOnGround.class, PlaceOnGround::new);
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Starting plugin " + this.getName());
        this.getEntityStoreRegistry().registerSystem(new RegisterPlaceOnGroundSystem(
                ItemComponent.getComponentType(), BlockTypeListAsset.getAssetMap().getAsset("Handheld_Torches")));
        this.getEntityStoreRegistry().registerSystem(new PlaceThrownTorchSystem());
    }

    public ComponentType<EntityStore, PlaceOnGround> getPlaceOnGroundComponentType() {
        return placeOnGroundComponentType;
    }
}