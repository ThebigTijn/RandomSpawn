package nl.ThebigTijn.randomspawn.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import nl.ThebigTijn.randomspawn.util.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class ModEntityDataSaverMixin implements IEntityDataSaver {
	private NbtCompound persistentData;

	@Override
	public NbtCompound getPersistentData() {
		if (persistentData == null) {
			persistentData = new NbtCompound();
		}
		return persistentData;
	}

	@Inject(method = "writeNbt", at = @At("HEAD"))
	protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		if(persistentData != null) {
			nbt.put("randomspawn.data", persistentData);
		}
	}

	@Inject(method = "readNbt", at = @At("HEAD"))
	protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
		if (nbt.contains("randomspawn.data", 10)) {
			persistentData = nbt.getCompound("randomspawn.data");
		}
	}
}