package nl.ThebigTijn.randomspawn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
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
			if (isFirstJoin((IEntityDataSaver) handler.getPlayer()) & ModConfigs.SpawnOnFirstJoin) {

				teleportPlayerToRandomLocation(handler.getPlayer());
				markPlayerAsJoined((IEntityDataSaver) handler.getPlayer());
			}
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (!playerHasRespawnPoint(newPlayer) & ModConfigs.SpawnOnRespawn) {
				new Thread(() -> {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					teleportPlayerToRandomLocation(newPlayer);
				}).start();
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

	private boolean playerHasRespawnPoint(ServerPlayerEntity player) {
		return player.getSpawnPointPosition() != null;
	}

	private void teleportPlayerToRandomLocation(ServerPlayerEntity player) {
		final Random[] random = {new Random()};
		World world = player.getWorld();
		final int[] x = {minX + random[0].nextInt(maxX - minX + 1)};
		final int[] z = {minZ + random[0].nextInt(maxZ - minZ + 1)};
		final BlockPos[] pos = {findSolidGround(world, x[0], z[0])};
		new Thread(() -> {
			while (!world.getBlockState(pos[0]).isAir()) {
				random[0] = new Random();
				x[0] = minX + random[0].nextInt(maxX - minX + 1);
				z[0] = minZ + random[0].nextInt(maxZ - minZ + 1);
				pos[0] = findSolidGround(world, x[0], z[0]);
				if (pos[0] != null && world.getBlockState(pos[0]).isAir()) {
					player.teleport((ServerWorld) world, pos[0].getX() + 0.5, pos[0].getY(), pos[0].getZ() + 0.5, player.getYaw(), player.getPitch());
				}
			}
		}).start();
	}

	private BlockPos findSolidGround(World world, int x, int z) {
		for (int y = world.getTopY() - 1; y > 0; y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if (world.getBlockState(pos).isSolidBlock(world, pos)) {
				return pos.up();
			}
		}
		return null;
	}
}