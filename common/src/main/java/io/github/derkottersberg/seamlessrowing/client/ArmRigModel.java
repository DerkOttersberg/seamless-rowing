package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.*;
import java.lang.Math;
import java.util.*;

public final class ArmRigModel extends Model<ArmRigModel.DrawState> {
    public record Bone(Vector3f shoulder,Quaternionf upper,Quaternionf lower,boolean visible,boolean sleeve) {
        public Bone { shoulder=new Vector3f(shoulder);upper=new Quaternionf(upper);lower=new Quaternionf(lower); }
        @Override public Vector3f shoulder() { return new Vector3f(shoulder); }
        @Override public Quaternionf upper() { return new Quaternionf(upper); }
        @Override public Quaternionf lower() { return new Quaternionf(lower); }
    }
    public record DrawState(Bone left,Bone right,boolean firstPerson) {}
    private final ModelPart left,right,leftLower,rightLower;
    public ArmRigModel(PlayerModel model) {
        super(root(model),RenderTypes::entityCutout);
        left=root.getChild("left"); right=root.getChild("right");
        leftLower=left.getChild("lower"); rightLower=right.getChild("lower");
    }
    private static ModelPart root(PlayerModel model) {
        return new ModelPart(List.of(),Map.of("left",arm(model.leftArm,model.leftSleeve),"right",arm(model.rightArm,model.rightSleeve)));
    }
    public static ModelPart arm(ModelPart source,ModelPart sleeve) {
        var cube=((io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess)(Object)source).rowingCubes().getFirst();
        float center=(cube.minX+cube.maxX)*.5f;
        ModelPart lowerSleeve=new ModelPart(ArmMesh.slice(sleeve,false,center),Map.of());
        ModelPart upperSleeve=new ModelPart(ArmMesh.slice(sleeve,true,center),Map.of());
        ModelPart lower=new ModelPart(ArmMesh.slice(source,false,center),Map.of("sleeve",lowerSleeve));
        lower.setPos(0,6,0);
        var distal=new ModelPart(ArmMesh.firstPersonUpper(source,center),Map.of());
        var distalSleeve=new ModelPart(ArmMesh.firstPersonUpper(sleeve,center),Map.of());
        ModelPart upper=new ModelPart(ArmMesh.slice(source,true,center),Map.of("lower",lower,"sleeve",upperSleeve,
                "distal",distal,"distal_sleeve",distalSleeve));
        return upper;
    }
    @Override public void setupAnim(DrawState state) {
        apply(left,leftLower,state.left,state.firstPerson); apply(right,rightLower,state.right,state.firstPerson);
    }
    public static void apply(ModelPart upper,ModelPart lower,Bone bone,boolean firstPerson) {
        upper.visible=bone.visible;
        upper.setPos(bone.shoulder.x,bone.shoulder.y,bone.shoulder.z);
        rotate(upper,bone.upper); rotate(lower,bone.lower);
        upper.skipDraw=firstPerson;
        upper.getChild("distal").visible=firstPerson;
        upper.getChild("distal_sleeve").visible=firstPerson&&bone.sleeve;
        upper.getChild("sleeve").visible=bone.sleeve&&!firstPerson;
        lower.getChild("sleeve").visible=bone.sleeve;
    }
    private static void rotate(ModelPart part,Quaternionf q) {
        Vector3f angles=q.getEulerAnglesZYX(new Vector3f()); part.setRotation(angles.x,angles.y,angles.z);
    }
}
