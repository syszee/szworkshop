package me.aurelium.szworkshop.sound;

import me.aurelium.szworkshop.SZWorkshop;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZSoundEvents {
	public static SoundEvent QUIVER_CREATE = new SoundEvent(new Identifier(SZWorkshop.MODID, "quiver_create"));
	public static SoundEvent QUIVER_DAMAGE = new SoundEvent(new Identifier(SZWorkshop.MODID, "quiver_damage"));
	public static SoundEvent QUIVER_DEATH = new SoundEvent(new Identifier(SZWorkshop.MODID, "quiver_death"));

	public static void initialize() {
		Registry.register(Registry.SOUND_EVENT, QUIVER_CREATE.getId(), QUIVER_CREATE);
		Registry.register(Registry.SOUND_EVENT, QUIVER_DAMAGE.getId(), QUIVER_DAMAGE);
		Registry.register(Registry.SOUND_EVENT, QUIVER_DEATH.getId(), QUIVER_DEATH);
	}
}
