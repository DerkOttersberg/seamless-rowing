package io.github.derkottersberg.seamlessrowing.client;

/** Original periodic pose curve. Minecraft's paddle phase retimes it to the actual stroke. */
public final class StrokeProfile {
    private static final float[] DEFAULT={0.035f,0.085f,0.045f,-0.035f,-0.075f,-0.040f,0.0f,0.020f};
    private static volatile float[] pitch=DEFAULT.clone();
    private StrokeProfile() {}
    public static float lean(float phase) {
        float[] PITCH=pitch;
        double fraction=phase/(Math.PI*2); fraction-=Math.floor(fraction);
        double position=fraction*PITCH.length; int i=(int)position; float t=(float)(position-i);
        float a=PITCH[(i+7)%8],b=PITCH[i],c=PITCH[(i+1)%8],d=PITCH[(i+2)%8];
        return .5f*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t);
    }
    public static float[] prepare(net.minecraft.server.packs.resources.ResourceManager resources) {
        var id=net.minecraft.resources.Identifier.fromNamespaceAndPath("seamlessrowing","animations/fixed_seat.json");
        try(var reader=resources.getResourceOrThrow(id).openAsReader()) {
            return validate(com.google.gson.JsonParser.parseReader(reader).getAsJsonObject());
        } catch(Exception invalid) {
            org.slf4j.LoggerFactory.getLogger("Seamless Rowing").warn("Invalid rowing pose clip; using original poses",invalid);
            return DEFAULT.clone();
        }
    }
    public static float[] validate(com.google.gson.JsonObject clip) {
        if(!clip.has("format") || !clip.get("format").isJsonPrimitive()
                || !clip.get("format").getAsJsonPrimitive().isNumber()
                || clip.get("format").getAsBigDecimal().compareTo(java.math.BigDecimal.ONE)!=0
                || !clip.has("interpolation") || !clip.get("interpolation").isJsonPrimitive()
                || !clip.get("interpolation").getAsJsonPrimitive().isString()
                || !clip.get("interpolation").getAsString().equals("catmullrom"))
            throw new IllegalArgumentException("Unsupported rowing clip format");
        var points=clip.getAsJsonArray("torso_pitch_radians");
        if(points.size()!=8) throw new IllegalArgumentException("Eight periodic keys required");
        float[] result=new float[8];
        for(int i=0;i<8;i++) {
            if(!points.get(i).isJsonPrimitive() || !points.get(i).getAsJsonPrimitive().isNumber())
                throw new IllegalArgumentException("Numeric pose keys required");
            result[i]=points.get(i).getAsFloat();
            if(!Float.isFinite(result[i])||Math.abs(result[i])>.2f) throw new IllegalArgumentException("Pose outside fixed-seat bounds");
        }
        return result;
    }
    public static void apply(float[] points) {
        if(points.length!=8) throw new IllegalArgumentException("Eight periodic keys required");
        float[] copy=points.clone();
        for(float key:copy) if(!Float.isFinite(key)||Math.abs(key)>.2f)
            throw new IllegalArgumentException("Pose outside fixed-seat bounds");
        pitch=copy;
    }
}
