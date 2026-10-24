// SPDX-License-Identifier: GPL-3.0-only
// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.
package com.igoryok95.ic2raddons.control.tileentities;

import com.igoryok95.ic2raddons.control.renderers.RotationOffset;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Snapshot of what the baked panel model needs, taken on the main thread when the chunk mesh is
 * built.
 *
 * @param textureId face border variant (0-15), see findTexture()
 * @param color screen background color (ARGB)
 * @param rotation screen rotation of a panel facing up or down, null means north
 * @param offset sloped/thin geometry of an advanced panel, null for a full cube
 */
public record PanelRenderData(
    int textureId, int color, boolean powered, Direction rotation, RotationOffset offset) {
  public static final ModelProperty<PanelRenderData> PROPERTY = new ModelProperty<>();
}
