package io.github.derkottersberg.seamlessrowing.client;

import io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** A draw snapshot for the native equipment pipeline, including its trims and glint. */
public final class RowingArmorModel extends Model<RowingArmorModel.DrawState> {
    public record PartPose(float x,float y,float z,float rx,float ry,float rz,boolean visible) {
        static PartPose of(ModelPart p) { return new PartPose(p.x,p.y,p.z,p.xRot,p.yRot,p.zRot,p.visible); }
        void apply(ModelPart p) { p.setPos(x,y,z);p.setRotation(rx,ry,rz);p.visible=visible; }
    }
    public record DrawState(Map<String,PartPose> poses,ArmRigModel.Bone left,ArmRigModel.Bone right,boolean articulated,boolean firstPerson) {}
    private final boolean articulated;
    public RowingArmorModel(HumanoidModel<?> original,boolean articulated) {
        super(copyRoot(original,articulated),RenderTypes::armorCutoutNoCull);
        this.articulated=articulated;
    }
    private static ModelPart copyRoot(HumanoidModel<?> source,boolean articulated) {
        Map<String,ModelPart> parts=new HashMap<>();
        ((ModelPartAccess)(Object)source.root()).rowingChildren().forEach((name,part)->parts.put(name,copy(part)));
        if(articulated) {
            parts.put("left_arm",ArmRigModel.arm(source.leftArm,new ModelPart(List.of(),Map.of())));
            parts.put("right_arm",ArmRigModel.arm(source.rightArm,new ModelPart(List.of(),Map.of())));
        }
        return new ModelPart(List.of(),Map.copyOf(parts));
    }
    private static ModelPart copy(ModelPart source) {
        Map<String,ModelPart> children=new HashMap<>();
        ((ModelPartAccess)(Object)source).rowingChildren().forEach((name,part)->children.put(name,copy(part)));
        return new ModelPart(((ModelPartAccess)(Object)source).rowingCubes(),Map.copyOf(children));
    }
    public DrawState capture(HumanoidModel<?> original,PlayerModel player,RowingPose row,Matrix4f transform,boolean handsOnly) {
        Map<String,PartPose> poses=new HashMap<>();
        ((ModelPartAccess)(Object)original.root()).rowingChildren().forEach((name,part)->poses.put(name,PartPose.of(part)));
        poses.put("body",PartPose.of(player.body)); poses.put("head",PartPose.of(player.head));
        if(!articulated) { poses.put("left_arm",PartPose.of(player.leftArm));poses.put("right_arm",PartPose.of(player.rightArm)); }
        if(handsOnly) poses.replaceAll((name,pose)->new PartPose(pose.x,pose.y,pose.z,pose.rx,pose.ry,pose.rz,
                name.equals("left_arm")&&row.ownLeft() || name.equals("right_arm")&&row.ownRight()));
        return new DrawState(Map.copyOf(poses),articulated ? armPose(player.leftArm,row,true,transform,handsOnly) : null,
                articulated ? armPose(player.rightArm,row,false,transform,handsOnly) : null,articulated,handsOnly);
    }
    private static ArmRigModel.Bone armPose(ModelPart arm,RowingPose row,boolean left,Matrix4f transform,boolean handsOnly) {
        if(row.owns(left)) return RowingArmLayer.solve(arm,row,left,transform,false);
        var cube=((ModelPartAccess)(Object)arm).rowingCubes().getFirst();
        var rotation=new Quaternionf().rotationZYX(arm.zRot,arm.yRot,arm.xRot);
        var shoulder=rotation.transform(new Vector3f((cube.minX+cube.maxX)/2,-2,0)).add(arm.x,arm.y,arm.z);
        return new ArmRigModel.Bone(shoulder,rotation,new Quaternionf(),!handsOnly&&arm.visible,false);
    }
    @Override public void setupAnim(DrawState state) {
        state.poses.forEach((name,pose)->pose.apply(root.getChild(name)));
        if(state.articulated) {
            var left=root.getChild("left_arm");var right=root.getChild("right_arm");
            ArmRigModel.apply(left,left.getChild("lower"),state.left,state.firstPerson);
            ArmRigModel.apply(right,right.getChild("lower"),state.right,state.firstPerson);
        }
    }
}
