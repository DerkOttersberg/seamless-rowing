import org.joml.*;
class EulerProbe {
    public static void main(String[] args){
        Quaternionf q=new Quaternionf().rotationX(-1.6f).rotateY(.08f);
        Vector3f a=q.getEulerAnglesZYX(new Vector3f());
        Quaternionf nativeQ=new Quaternionf().rotationZYX(a.z,a.y,a.x);
        System.out.println(q+" -> "+a+" -> "+nativeQ);
        System.out.println(new Vector3f(0,6,0).rotate(q).sub(new Vector3f(0,6,0).rotate(nativeQ)));
    }
}
