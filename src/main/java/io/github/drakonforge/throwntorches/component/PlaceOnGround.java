package io.github.drakonforge.throwntorches.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.drakonforge.throwntorches.ThrownTorchesPlugin;

public class PlaceOnGround implements Component<EntityStore> {
    private double previousVelocityY;

    public static ComponentType<EntityStore, PlaceOnGround> getComponentType() {
        return ThrownTorchesPlugin.getInstance().getPlaceOnGroundComponentType();
    }

    public PlaceOnGround() {
        this.previousVelocityY = 0.0;
    }

    public double getPreviousVelocityY() {
        return previousVelocityY;
    }

    public void setPreviousVelocityY(double previousVelocityY) {
        this.previousVelocityY = previousVelocityY;
    }

    public Component<EntityStore> clone() {
        PlaceOnGround clone = new PlaceOnGround();
        clone.previousVelocityY = previousVelocityY;
        return clone;
    }
}