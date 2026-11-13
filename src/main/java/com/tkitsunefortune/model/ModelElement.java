package com.tkitsunefortune.model;

import org.bukkit.Material;

public class ModelElement {

    private final String name;
    private final float fromX;
    private final float fromY;
    private final float fromZ;
    private final float toX;
    private final float toY;
    private final float toZ;
    private final String rotationAxis;
    private final float rotationAngleDegrees;
    private final float originX;
    private final float originY;
    private final float originZ;
    private final Material material;
    private final boolean emissive;

    public ModelElement(
            String name,
            float fromX, float fromY, float fromZ,
            float toX, float toY, float toZ,
            String rotationAxis, float rotationAngleDegrees,
            float originX, float originY, float originZ,
            Material material,
            boolean emissive
    ) {
        this.name = name;
        this.fromX = fromX;
        this.fromY = fromY;
        this.fromZ = fromZ;
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
        this.rotationAxis = rotationAxis != null ? rotationAxis : "z";
        this.rotationAngleDegrees = rotationAngleDegrees;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.material = material;
        this.emissive = emissive;
    }

    public String getName() {
        return name;
    }

    public float getFromX() {
        return fromX;
    }

    public float getFromY() {
        return fromY;
    }

    public float getFromZ() {
        return fromZ;
    }

    public float getToX() {
        return toX;
    }

    public float getToY() {
        return toY;
    }

    public float getToZ() {
        return toZ;
    }

    public String getRotationAxis() {
        return rotationAxis;
    }

    public float getRotationAngleDegrees() {
        return rotationAngleDegrees;
    }

    public float getOriginX() {
        return originX;
    }

    public float getOriginY() {
        return originY;
    }

    public float getOriginZ() {
        return originZ;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isEmissive() {
        return emissive;
    }
}

