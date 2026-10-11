package io.github.derkottersberg.seamlessrowing.client;
/** CEM boat replacement rigs have unverified grip geometry and retain their own rendering. */
public final class BoatRigResourcePolicy {
    private BoatRigResourcePolicy(){}
    public static boolean replacement(String namespace,String path){
        if(!namespace.equals("minecraft")||!path.endsWith(".jem"))return false;
        String name=path.substring(path.lastIndexOf('/')+1,path.length()-4);
        return name.contains("boat")||name.contains("raft");
    }
}
