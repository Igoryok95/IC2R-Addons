package com.igoryok95.ic2raddons.bridge;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Server-only test actor; optional mods must not send unnegotiated client packets to it. */
public final class TestPlayers {
  public static ServerPlayer create(GameTestHelper helper) {
    ServerPlayer player =
        new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ic2r-addons-test"));
    player.setGameMode(GameType.SURVIVAL);
    var pos = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5));
    player.setPos(pos.x, pos.y, pos.z);
    return player;
  }

  private TestPlayers() {}
}
