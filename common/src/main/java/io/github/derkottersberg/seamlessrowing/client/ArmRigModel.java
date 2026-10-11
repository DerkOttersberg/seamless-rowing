package io.github.derkottersberg.seamlessrowing.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.RenderType;
import org.joml.*;
import java.lang.Math;
import java.util.*;
import io.github.derkottersberg.seamlessrowing.math.ArmSurface;

public final class ArmRigModel extends Model {
    public record Bone(Vector3f shoulder,Quaternionf upper,Quaternionf lower,boolean visible,boolean sleeve) {
        public Bone { shoulder=new Vector3f(shoulder);upper=new Quaternionf(upper);lower=new Quaternionf(lower); }
        @Override public Vector3f shoulder() { return new Vector3f(shoulder); }
        @Override public Quaternionf upper() { return new Quaternionf(upper); }
        @Override public Quaternionf lower() { return new Quaternionf(lower); }
    }
    public record DrawState(Bone left,Bone right,boolean firstPerson) {}
    private final ModelPart root,left,right,leftLower,rightLower;
    private static final Map<ModelPart,TexturedSurface> SURFACES=new WeakHashMap<>();
    public ArmRigModel(PlayerModel model) {
        super(RenderType::entityCutoutNoCull);
        root=root(model);
        left=root.getChild("left"); right=root.getChild("right");
        leftLower=left.getChild("lower"); rightLower=right.getChild("lower");
    }
    public ModelPart root(){return root;}
    @Override public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack stack,com.mojang.blaze3d.vertex.VertexConsumer consumer,int light,int overlay,int color){root.render(stack,consumer,light,overlay,color);}
    private static ModelPart root(PlayerModel model) {
        return new ModelPart(List.of(),Map.of("left",arm(model.leftArm,model.leftSleeve),"right",arm(model.rightArm,model.rightSleeve)));
    }
    public static ModelPart arm(ModelPart source,ModelPart sleeve) {
        var cube=((io.github.derkottersberg.seamlessrowing.mixin.ModelPartAccess)(Object)source).rowingCubes().get(0);
        float center=(cube.minX+cube.maxX)*.5f;
        ModelPart upperSleeve=surface(sleeve,center,Float.NEGATIVE_INFINITY);
        // Empty joint marker retained for native contact probes and equipment snapshots.
        ModelPart lower=new ModelPart(List.of(),Map.of());
        lower.setPos(0,6,0);
        var distal=surface(source,center,2);
        var distalSleeve=surface(sleeve,center,2);
        var mesh=new TexturedSurface(source,center,2,.5f,Float.NEGATIVE_INFINITY);
        ModelPart upper=new ModelPart(mesh.cubes(),Map.of("lower",lower,"sleeve",upperSleeve,"distal",distal,"distal_sleeve",distalSleeve));
        SURFACES.put(upper,mesh);
        return upper;
    }
    private static ModelPart surface(ModelPart source,float center,float cutoff) {
        var mesh=new TexturedSurface(source,center,2,.5f,cutoff);
        var part=new ModelPart(mesh.cubes(),Map.of());SURFACES.put(part,mesh);return part;
    }
    public void setupAnim(DrawState state) {
        apply(left,leftLower,state.left,state.firstPerson); apply(right,rightLower,state.right,state.firstPerson);
    }
    public static void apply(ModelPart upper,ModelPart lower,Bone bone,boolean firstPerson) {
        upper.visible=bone.visible;
        upper.setPos(bone.shoulder.x,bone.shoulder.y,bone.shoulder.z);
        rotate(upper,bone.upper); rotate(lower,bone.lower);
        if(bone.visible) {
            var warp=new ArmSurface(bone.lower);
            SURFACES.get(firstPerson?upper.getChild("distal"):upper).deform(warp::point);
            if(bone.sleeve) SURFACES.get(upper.getChild(firstPerson?"distal_sleeve":"sleeve")).deform(warp::point);
        }
        upper.skipDraw=firstPerson;
        upper.getChild("distal").visible=firstPerson;
        upper.getChild("distal_sleeve").visible=firstPerson&&bone.sleeve;
        upper.getChild("sleeve").visible=bone.sleeve&&!firstPerson;
    }
    private static void rotate(ModelPart part,Quaternionf q) {
        Vector3f angles=q.getEulerAnglesZYX(new Vector3f()); part.setRotation(angles.x,angles.y,angles.z);
    }
}
