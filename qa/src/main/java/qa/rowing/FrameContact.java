package qa.rowing;

import org.joml.Vector3f;
import java.util.*;

/** Independent comparison of the native paddle and submitted mesh transforms in the same frame. */
public final class FrameContact {
    private static final Map<Boolean,Vector3f> hands=new HashMap<>(),handles=new HashMap<>();
    private static final Set<Boolean> measured=new HashSet<>();
    private static int samples;
    private static double maximum;
    private static int duplicates;
    private static final Map<String,Double> byView=new HashMap<>();
    private static final Map<String,Integer> byCase=new LinkedHashMap<>();
    private static final Map<Integer,Integer> phaseBins=new TreeMap<>();
    private static final Map<Boolean,Float> phases=new HashMap<>();
    public static void begin() { hands.clear();handles.clear();measured.clear();phases.clear(); }
    public static void phase(boolean left,float phase){phases.put(left,phase);}
    public static void hand(boolean left,Vector3f position) { if(hands.containsKey(left))duplicates++;hands.put(left,position);compare(left); }
    public static void handle(boolean left,Vector3f position) { handles.put(left,position);compare(left); }
    private static void compare(boolean left) {
        if(!hands.containsKey(left)||!handles.containsKey(left)||!measured.add(left)) return;
        var delta=new Vector3f(hands.get(left)).sub(handles.get(left)).mul(16);
        double error=delta.length();samples++;maximum=Math.max(maximum,error);
        if(phases.containsKey(left)) {
            double cycle=phases.get(left)/(2*Math.PI);cycle-=Math.floor(cycle);
            phaseBins.merge(Math.min(7,(int)(cycle*8)),1,Integer::sum);
        }
        if(MatrixScenario.current()!=null) byCase.merge(MatrixScenario.current(),1,Integer::sum);
        String view=System.getProperty("qa.rowing.role","").equals("observer") ? "OBSERVER" : net.minecraft.client.Minecraft.getInstance().options.getCameraType().name();
        double previous=byView.getOrDefault(view,-1d);
        if(error>previous+.01) System.out.println("NATIVE_CONTACT view="+view+" left="+left+" error="+error+" delta="+delta);
        byView.merge(view,error,Math::max);
    }
    public static int samples() { return samples; }
    public static double maximum() { return maximum; }
    public static Map<String,Double> views() { return Map.copyOf(byView); }
    public static Map<String,Integer> cases() { return Map.copyOf(byCase); }
    public static Map<Integer,Integer> phaseBins(){return Map.copyOf(phaseBins);}
    public static int duplicates(){return duplicates;}
}
