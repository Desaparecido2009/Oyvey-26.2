package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.commands.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Register;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.inventory.InventoryUtil;
import me.alpha432.oyvey.util.inventory.Result;
import me.alpha432.oyvey.util.models.Angles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static me.alpha432.oyvey.util.chat.SimpleSignature.from;

@Register
public class ItemFrameDupeModule extends Module {
    private final Setting<Integer> distance = num("Distance", 5, 1, 6);
    private final Setting<Boolean> backOfPiston = bool("BackOfPiston", true);
    private final Setting<Integer> delay = num("Delay", 1, 0, 10);
    private final Setting<Boolean> rotate = bool("Rotate", true);
    private final Setting<Boolean> swapBack = bool("SwapBack", true);

    private int timer;

    public ItemFrameDupeModule() {
        super("Assists with the item frame dupe by placing item frames on piston heads.", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer = delay.getValue();
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (timer > 0) {
            timer--;
            return;
        }

        Result frames = InventoryUtil.find(
                stack -> stack.is(Items.ITEM_FRAME) || stack.is(Items.GLOW_ITEM_FRAME),
                InventoryUtil.HOTBAR_SCOPE
        );

        if (!frames.found()) {
            Command.sendMessage("{red} No item frames found in hotbar.", from(getName()));
            disable();
            return;
        }

        timer = delay.getValue();

        BlockPos center = mc.player.blockPosition();
        int range = distance.getValue();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x * x + y * y + z * z > range * range) continue;

                    BlockPos pistonPos = center.offset(x, y, z);
                    if (!(mc.level.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock)) continue;
                    if (!place(pistonPos, frames)) continue;
                    return;
                }
            }
        }
    }

    private boolean place(BlockPos pistonPos, Result frames) {
        Direction facing = mc.level.getBlockState(pistonPos).getValue(DirectionalBlock.FACING);
        Direction attach = backOfPiston.getValue() ? facing.getOpposite() : facing;
        BlockPos framePos = pistonPos.relative(attach);

        if (!mc.level.getBlockState(framePos).canBeReplaced()) return false;
        if (!mc.level.getEntitiesOfClass(ItemFrame.class, new AABB(framePos)).isEmpty()) return false;

        Vec3 hit = Vec3.atCenterOf(pistonPos).add(attach.getStepX() * 0.5, attach.getStepY() * 0.5, attach.getStepZ() * 0.5);
        BlockHitResult hitResult = new BlockHitResult(hit, attach, pistonPos, false);

        Runnable action = () -> useFrame(hitResult, frames);

        if (rotate.getValue()) {
            OyVey.rotationManager.motion(Angles.pos(hit), action);
        } else {
            action.run();
        }

        return true;
    }

    private void useFrame(BlockHitResult hitResult, Result frames) {
        if (swapBack.getValue()) {
            InventoryUtil.withSwap(frames, result -> useItemOn(result.hand(), hitResult));
        } else if (InventoryUtil.swap(frames)) {
            useItemOn(frames.hand(), hitResult);
        }
    }

    private void useItemOn(InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hitResult);
        if (result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE) {
            mc.player.connection.send(new ServerboundSwingPacket(hand));
        }
    }
}
