from pathlib import Path
import os,re,subprocess,sys
profile=sys.argv[1]
if not re.fullmatch(r'client-[a-zA-Z0-9]+',profile):raise ValueError('Owned profile required')
for proc in Path('/proc').iterdir():
    if not proc.name.isdigit():continue
    try:
        args=(proc/'cmdline').read_bytes().split(b'\0')
        if args[0]!=b'/opt/rowing-java-test/bin/java' or ('/mnt/d/SeamlessRowingQA-20261011/'+profile).encode() not in args:continue
        env=dict(e.split('=',1) for e in (proc/'environ').read_bytes().decode().split('\0') if '=' in e)
        if not re.fullmatch(r':[1-9][0-9]*',env.get('DISPLAY','')) or not env.get('XAUTHORITY') or env.get('WAYLAND_DISPLAY'):raise RuntimeError('Isolation lost')
        windows=subprocess.check_output(['xdotool','search','--pid',proc.name,'--name','Minecraft'],env=env,text=True).splitlines()
        subprocess.run(['xdotool','windowfocus','--sync',windows[0]],env=env,check=True)
        subprocess.run(['xdotool','windowraise',windows[0]],env=env,check=True)
        subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-f','x11grab','-i',env['DISPLAY'],'-frames:v','1','-y','/mnt/d/SeamlessRowingQA-20261011/'+profile+'/private-display.png'],env=env,check=True)
        if len(sys.argv)>2 and sys.argv[2]=='escape':subprocess.run(['xdotool','key','--window',windows[0],'Escape'],env=env,check=True)
        print('Captured private QA display',env['DISPLAY']);break
    except FileNotFoundError:continue
else:raise RuntimeError('Owned client not found')
