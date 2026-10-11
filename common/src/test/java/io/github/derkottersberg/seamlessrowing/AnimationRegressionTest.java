package io.github.derkottersberg.seamlessrowing;

import io.github.derkottersberg.seamlessrowing.math.*;
import io.github.derkottersberg.seamlessrowing.client.PaddleTrack;
import io.github.derkottersberg.seamlessrowing.client.StrokeProfile;
import io.github.derkottersberg.seamlessrowing.client.ControllerEligibility;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationRegressionTest {
    @Test void passengersAndInvalidControllersNeverOwnRowing() {
        for(int flags=0;flags<64;flags++) {
            var policy=new ControllerEligibility((flags&1)!=0,(flags&2)!=0,(flags&4)!=0,
                    (flags&8)!=0,(flags&16)!=0,(flags&32)!=0);
            assertEquals(flags==57,policy.allowed());
        }
    }
    @Test void bothElbowsKeepBoneLengthsAndHitReachableTargets() {
        for(int side:new int[]{-1,1}) for(int frame=0;frame<256;frame++) {
            double phase=frame*Math.PI/128;
            Point3 shoulder=new Point3(side*6,0,0);
            Point3 target=new Point3(side*(5+2*Math.sin(phase)),5+Math.cos(phase),-4+Math.sin(phase));
            var result=ArmSolver.solve(shoulder,target,new Point3(side,.4,-.3),6,5);
            assertEquals(6,result.elbow().subtract(shoulder).length(),1e-8);
            assertEquals(5,result.hand().subtract(result.elbow()).length(),1e-8);
            assertTrue(result.error()<1e-7);
            assertTrue(side*result.elbow().subtract(shoulder).x()>-6);
        }
    }
    @Test void unreachableTargetsNeverStretchBonesOrGenerateNaNs() {
        for(Point3 target:new Point3[]{Point3.ZERO,new Point3(0,80,0),new Point3(0,-80,0)}) {
            var result=ArmSolver.solve(Point3.ZERO,target,new Point3(0,1,0),6,5);
            assertEquals(6,result.elbow().length(),1e-8);
            assertEquals(5,result.hand().subtract(result.elbow()).length(),1e-8);
            assertTrue(Double.isFinite(result.error()));
        }
    }
    @Test void samplingDoesNotAdvanceTransitionsAndStopReturnsToRest() {
        PaddleTrack track=new PaddleTrack();
        for(int tick=0;tick<20;tick++) track.tick(true,tick*.3926991f);
        assertEquals(1,track.weight(1));
        var expected=track.sample(6.2f,.5f);
        for(int i=0;i<100;i++) assertEquals(expected,track.sample(6.2f,.5f));
        var before=track.sample(19*.3926991f,1);
        track.tick(false,0);
        assertEquals(before.pitch(),track.sample(0,0).pitch(),1e-6);
        for(int tick=0;tick<4;tick++) track.tick(false,0);
        assertEquals(0,track.weight(1));
        assertEquals(PaddlePose.at(0),track.sample(0,1));
    }
    @Test void torsoCurveIsPeriodicWithContinuousWrapVelocity() {
        float wrap=(float)(Math.PI*2),epsilon=.0001f;
        assertEquals(StrokeProfile.lean(0),StrokeProfile.lean(wrap),1e-6);
        float before=(StrokeProfile.lean(0)-StrokeProfile.lean(-epsilon))/epsilon;
        float after=(StrokeProfile.lean(epsilon)-StrokeProfile.lean(0))/epsilon;
        assertEquals(before,after,.002);
    }
    @Test void nonfiniteCoordinatesAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new Point3(Double.NaN,0,0));
        assertThrows(IllegalArgumentException.class,()->ArmSolver.solve(Point3.ZERO,Point3.ZERO,Point3.ZERO,0,5));
    }
    @Test void resourceClipsRejectMalformedKeysAndDoNotExposeMutableSnapshots() {
        String valid="{\"format\":1,\"interpolation\":\"catmullrom\",\"torso_pitch_radians\":[0.035,0.085,0.045,-0.035,-0.075,-0.04,0,0.02]}";
        float[] keys=StrokeProfile.validate(com.google.gson.JsonParser.parseString(valid).getAsJsonObject());
        StrokeProfile.apply(keys);keys[0]=.2f;
        assertEquals(.035f,StrokeProfile.lean(0),1e-6);
        for(String invalid:new String[]{valid.replace("\"format\":1","\"format\":1.5"),valid.replace("0.085","0.3"),valid.replace("0.085","\"0.085\"")})
            assertThrows(IllegalArgumentException.class,()->StrokeProfile.validate(com.google.gson.JsonParser.parseString(invalid).getAsJsonObject()));
        assertThrows(IllegalArgumentException.class,()->StrokeProfile.apply(new float[]{Float.NaN}));
    }
}
