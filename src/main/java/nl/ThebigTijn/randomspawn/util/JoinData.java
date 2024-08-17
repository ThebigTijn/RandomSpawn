package nl.ThebigTijn.randomspawn.util;

import net.minecraft.nbt.NbtCompound;

public class JoinData {

	public static boolean isFirstJoin(IEntityDataSaver player) {
		NbtCompound nbt = player.getPersistentData();
		boolean joined = nbt.getBoolean("joined");
		return !joined;
	}

	public static void markPlayerAsJoined(IEntityDataSaver player) {
		NbtCompound nbt = player.getPersistentData();
		nbt.putBoolean("joined", true);
	}

}
