package io.github.derkottersberg.seamlessrowing.client;

/** Recognizes the two publicly released 1.1 rigs; no resource-pack assets are bundled. */
public final class FreshPlayerRig {
    private FreshPlayerRig() {}
    public static boolean known(String classic, String slim) {
        return "116ea0b2bef92627c8dede9659a6a2ce19bd05e129280dc76581a92a9a6347a9".equals(classic)
                && "9e7fee9d3b0ee031137c216b16a69556ff11b1628d6075d12d027f5151cbdb04".equals(slim);
    }
}
