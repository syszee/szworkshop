package me.aurelium.szworkshop.item;

import me.aurelium.szworkshop.SZWorkshop;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZItems {
	public static Item GLOW_SQUID_BUCKET = Registry.register(Registry.ITEM,
		SZWorkshop.id( "glow_squid_bucket"),
		new EntityBucketItem(EntityType.GLOW_SQUID, Fluids.WATER, SoundEvents.ITEM_BUCKET_EMPTY_FISH, new FabricItemSettings().maxCount(1).group(SZWorkshop.SZ_GROUP))
	);

	public static void initialize() {

	}
}
