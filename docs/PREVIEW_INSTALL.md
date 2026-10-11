# Install the 26.3 Fabric preview

1. Use a Minecraft **26.3** client, **Java 25**, Fabric Loader **0.19.5** and Fabric API **0.161.0+26.3**.
2. Put `seamless-rowing-0.1.0-preview.1+mc26.3-fabric.jar` in that client's `mods` directory. Do not install the common, sources or QA JARs.
3. Enter a world or a vanilla server, mount a vanilla boat/raft as its controlling rider, and use normal boat controls. Other viewers need Seamless Rowing on their own clients to see the improved animation.
4. Review first person, third person behind, and third person facing the player. Try forward movement, turning, stopping, backwards-only movement, attacking and using an item. These are the requested feedback checkpoint before ports.
5. Optionally bind **Open rowing settings** under Options → Controls → Key Binds → Seamless Rowing. Its default is unbound. Mod Menu provides the same client settings screen.

No SeamlessLib or animation framework is required. Mod Menu and the tested animation integrations are optional. The preview deliberately uses conservative fallbacks for unknown custom rigs. With 3D Skin Layers, first-person rendering stays with that mod and third-person rowing uses rigid arms.

The preview's SHA-256 is:

```
f49494a27cbd9701b6b031844698dee224ebffd3f8e12240152147c3305dd855
```

This is a preview, not a production release. The acceptance report identifies the measured tests and remaining hardware/shader and release-matrix gates. The other ten builds wait for animation feedback. GitHub publication and CurseForge uploads have not been requested for this product.
