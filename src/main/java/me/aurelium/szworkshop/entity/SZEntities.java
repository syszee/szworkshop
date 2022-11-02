package me.aurelium.szworkshop.entity;

import me.aurelium.szworkshop.SZWorkshop;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZEntities {
	public static final EntityType<QuiverEntity> QUIVER = Registry.register(
			Registry.ENTITY_TYPE,
			SZWorkshop.id( "quiver"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, QuiverEntity::new).dimensions(EntityDimensions.fixed(1f, 1.5f)).build()
	);

	public static void initialize() {
		FabricDefaultAttributeRegistry.register(QUIVER, QuiverEntity.createMobAttributes());
	}
}
