"""Private SDL3 context probe using the exact packaged Minecraft native library."""
import ctypes as c,json
from pathlib import Path
s=c.CDLL('/mnt/d/SeamlessRowingPortsQA-20261011/client-263-fabric-shader-f/natives/libSDL3.so')
s.SDL_Init.argtypes=[c.c_uint];s.SDL_Init.restype=c.c_bool
s.SDL_GetError.restype=c.c_char_p
s.SDL_GL_SetAttribute.argtypes=[c.c_int,c.c_int];s.SDL_GL_SetAttribute.restype=c.c_bool
s.SDL_CreateWindow.argtypes=[c.c_char_p,c.c_int,c.c_int,c.c_uint64];s.SDL_CreateWindow.restype=c.c_void_p
s.SDL_GL_CreateContext.argtypes=[c.c_void_p];s.SDL_GL_CreateContext.restype=c.c_void_p
s.SDL_GL_DestroyContext.argtypes=[c.c_void_p];s.SDL_DestroyWindow.argtypes=[c.c_void_p]
assert s.SDL_Init(32),s.SDL_GetError()
results=[]
for srgb in (1,0):
    s.SDL_GL_ResetAttributes()
    for key,value in ((17,3),(18,3),(20,1),(19,2),(22,srgb)):assert s.SDL_GL_SetAttribute(key,value)
    window=s.SDL_CreateWindow(b'Private GL probe',320,480,2|8)
    context=s.SDL_GL_CreateContext(window) if window else None
    results.append(dict(srgb=srgb,window=bool(window),context=bool(context),error=s.SDL_GetError().decode()))
    if context:s.SDL_GL_DestroyContext(context)
    if window:s.SDL_DestroyWindow(window)
print(json.dumps(results,indent=2))
s.SDL_Quit()
