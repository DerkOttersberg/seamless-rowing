"""Always resume paused owned schedulers after bounded priority fixture completion."""
from pathlib import Path
import argparse,json,subprocess,time
root=Path('/mnt/d/SeamlessRowingPortsQA-20261011');qa=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--result');p.add_argument('--count',type=int);args=p.parse_args()
assert (args.result is None)==(args.count is None)
if args.result:assert Path(args.result).name==args.result and 1<=args.count<=20
assert (root/'paused-review-schedulers.json').is_file()
deadline=time.monotonic()+600
try:
    while time.monotonic()<deadline:
        finished=[]
        targets=[(args.result,args.count)] if args.result else [('edge-af-jobs-results.json',4),('cadence-ag-jobs-results.json',2)]
        for name,count in targets:
            file=root/name
            finished.append(file.is_file() and len(json.loads(file.read_text()))>=count)
        if all(finished):break
        time.sleep(5)
finally:
    subprocess.run(['python3',str(qa/'prioritize_visual_review.py'),'resume'],check=True)
