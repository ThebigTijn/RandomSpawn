package nl.ThebigTijn.randomspawn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.ThebigTijn.randomspawn.config.ModConfigs;
import nl.ThebigTijn.randomspawn.util.IEntityDataSaver;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;

import static nl.ThebigTijn.randomspawn.util.JoinData.isFirstJoin;
import static nl.ThebigTijn.randomspawn.util.JoinData.markPlayerAsJoined;

public class Randomspawn implements ModInitializer {

	int minX;
	int maxX;
	int minZ;
	int maxZ;

	@Override
	public void onInitialize() {
		loadConfig();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (isFirstJoin((IEntityDataSaver) handler.getPlayer())) {
				teleportPlayerToRandomLocation(handler.getPlayer());
				markPlayerAsJoined((IEntityDataSaver) handler.getPlayer());
			}
		});
	}

	private void loadConfig() {
		ModConfigs.registerConfigs();
		minX = ModConfigs.MinX;
		maxX = ModConfigs.MaxX;
		minZ = ModConfigs.MinZ;
		maxZ = ModConfigs.MaxZ;
	}


	private void teleportPlayerToRandomLocation(ServerPlayerEntity player) {
		Random random = new Random();
		World world = player.getWorld();
		BlockPos pos = null;

		while (pos == null) {
			int x = minX + random.nextInt(maxX - minX + 1);
			int z = minZ + random.nextInt(maxZ - minZ + 1);
			pos = findSolidGround(world, x, z);
		}

		player.teleport((ServerWorld) world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
	}

	private BlockPos findSolidGround(World world, int x, int z) {
		for (int y = world.getTopY() - 1; y > 0; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (world.getBlockState(pos).isSolidBlock(world, pos)) {
				BlockPos abovePos = pos.up();
				if (!world.getBlockState(abovePos).isLiquid()) {
					return abovePos;
				}
			}
		}
		return null;
	}

}