package io.github.derkottersberg.seamlessrowing;
import io.github.derkottersberg.seamlessrowing.math.ModelOrientation;
import org.joml.*;
import java.lang.Math;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ModelOrientationTest {
    @Test void nativeAnglesPreserveWristAndHingeAcrossCombinedRotations(){
        for(int i=0;i<1000;i++){
            var q=new Quaternionf().rotationX(-2f+i*.004f).rotateY(.12f*(float)Math.sin(i*.01)).rotateZ(.5f*(float)Math.cos(i*.02));
            var a=ModelOrientation.angles(q);var rendered=new Quaternionf().rotationZYX(a.z,a.y,a.x);
            for(var axis:new Vector3f[]{new Vector3f(0,6,0),new Vector3f(6,0,0)})
                assertTrue(new Vector3f(axis).rotate(q).distance(new Vector3f(axis).rotate(rendered))<.00001f);
        }
    }
}
