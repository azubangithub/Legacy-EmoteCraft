package com.zigythebird.playeranimcore.animation.layered;

import com.zigythebird.playeranimcore.bones.AdvancedBoneSnapshot;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class AnimationSnapshot implements IAnimation {
    private final Map<String, AdvancedBoneSnapshot> snapshots;

    public AnimationSnapshot(Map<String, AdvancedBoneSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public Map<String, AdvancedBoneSnapshot> snapshots() { return this.snapshots; }
    public Map<String, AdvancedBoneSnapshot> getSnapshots() { return this.snapshots; }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        if (snapshots.containsKey(bone.getName())) {
            return bone.copySnapshotSafe(snapshots.get(bone.getName()));
        }
        return bone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnimationSnapshot)) return false;
        AnimationSnapshot that = (AnimationSnapshot) o;
        return Objects.equals(snapshots, that.snapshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshots);
    }

    @Override
    public String toString() {
        return "AnimationSnapshot{" +
                "snapshots=" + snapshots +
                '}';
    }
}
