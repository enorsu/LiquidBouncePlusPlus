/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.potion.Potion

@ModuleInfo(name = "OldSprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class OldSprint : Module() {
    val allDirectionsValue: BoolValue = BoolValue("AllDirections", true)
    val blindnessValue: BoolValue = BoolValue("Blindness", true)
    val foodValue: BoolValue = BoolValue("Food", true)

    val checkServerSide: BoolValue = BoolValue("CheckServerSide", false)
    val checkServerSideGround: BoolValue = BoolValue("CheckServerSideOnlyGround", false)


    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking ||
            (blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness)) ||
            (foodValue.get() && !(mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying))
            || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
                    && !allDirectionsValue.get() && RotationUtils.targetRotation != null && RotationUtils.getRotationDifference(
                Rotation(
                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch
                )
            ) > 30)
        ) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8f) mc.thePlayer.isSprinting =
            true
    }
}